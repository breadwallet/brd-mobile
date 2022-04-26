/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/13/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.breadwallet.R
import com.breadwallet.breadbox.BreadBox
import com.breadwallet.databinding.ControllerScannerBinding
import com.breadwallet.logger.logDebug
import com.breadwallet.logger.logError
import com.breadwallet.tools.qrcode.QRCodeImageAnalysis
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.tools.util.Link
import com.breadwallet.tools.util.asLink
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.MainActivity
import com.breadwallet.util.CryptoUriParser
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transformLatest
import org.kodein.di.instance
import java.util.concurrent.Executors

private const val CAMERA_UI_UPDATE_MS = 100L

class ScannerController(
    args: Bundle? = null
) : BaseController(args) {

    interface Listener {
        fun onLinkScanned(link: Link)

        fun onRawTextScanned(text: String) = Unit
    }

    private val breadBox by instance<BreadBox>()
    private val uriParser by instance<CryptoUriParser>()
    private val binding by viewBinding(ControllerScannerBinding::inflate)
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private val mainExecutor = Main.asExecutor()
    private val backgroundExecutor by lazy { Executors.newSingleThreadExecutor() }
    private val cameraSelector by lazy {
        CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        val context = checkNotNull(applicationContext)

        val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
            try {
                startScanner(breadBox, uriParser)
            } catch (e: NoCameraException) {
                logError("No camera found, exiting scanner.")
                toastLong(R.string.Scanner_noCamera)
                router.popController(this)
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                BRConstants.CAMERA_REQUEST_ID
            )
        }
    }

    override fun onDetach(view: View) {
        cameraProviderFuture?.apply {
            addListener(
                {
                    get().unbindAll()
                },
                mainExecutor
            )
        }
        super.onDetach(view)
    }

    override fun onDestroy() {
        backgroundExecutor.shutdown()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BRConstants.CAMERA_REQUEST_ID) {
            when (grantResults.singleOrNull()) {
                PackageManager.PERMISSION_GRANTED -> startScanner(breadBox, uriParser)
                PackageManager.PERMISSION_DENIED, null -> router.popController(this)
            }
        }
    }

    private fun startScanner(breadBox: BreadBox, uriParser: CryptoUriParser) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext!!).apply {
            if (!(get().hasCamera(cameraSelector))) throw NoCameraException()

            addListener(
                {
                    val imageAnalysis = bindImageAnalyzer(get())

                    imageAnalysis.decodedTextFlow()
                        .mapLatest { text ->
                            text to text.asLink(
                                breadBox,
                                uriParser,
                                scanned = true
                            )
                        }
                        .flowOn(Default)
                        .transformLatest { (text, link) ->
                            if (link == null) {
                                logError("Found incompatible QR code")
                                showGuideError()
                            } else {
                                logDebug("Found compatible QR code $text -> $link")
                                binding.scanGuide.setImageResource(R.drawable.cameraguide)
                                emit(text to link)
                            }
                        }
                        .take(1)
                        .onEach { (text, link) ->
                            handleValidLink(text, link)
                        }
                        .flowOn(Main)
                        .launchIn(viewAttachScope)
                },
                mainExecutor
            )
        }
    }

    private fun bindImageAnalyzer(cameraProvider: ProcessCameraProvider): QRCodeImageAnalysis {
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(binding.previewView.surfaceProvider)
        }

        val imageAnalysis = QRCodeImageAnalysis(backgroundExecutor)

        cameraProvider.bindToLifecycle(
            activity as LifecycleOwner,
            cameraSelector,
            imageAnalysis.useCase,
            preview
        )

        return imageAnalysis
    }

    private fun handleValidLink(text: String, link: Link) {
        // Try calling the targetController to handle the link,
        // if no listener handles it, dispatch to MainActivity.
        val consumed: Unit? = findListener<Listener>()?.run {
            onRawTextScanned(text)
            onLinkScanned(link)
        }
        if (consumed == null) {
            Intent(applicationContext, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(MainActivity.EXTRA_DATA, text)
                .run(this::startActivity)
        }
        router.popController(this@ScannerController)
    }

    /** Display an error state for [CAMERA_UI_UPDATE_MS] then reset. */
    private suspend fun showGuideError() {
        binding.scanGuide.setImageResource(R.drawable.cameraguide_red)
        delay(CAMERA_UI_UPDATE_MS)
        binding.scanGuide.setImageResource(R.drawable.cameraguide)
    }

    class NoCameraException : Exception()
}
