/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.BuildConfig
import com.breadwallet.R
import com.breadwallet.databinding.ControllerExchangePartnerBrowserBinding
import com.breadwallet.logger.logInfo
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.ui.web.CameraController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class PartnerBrowserController(args: Bundle? = null) :
    ExchangeController.ChildController(args),
    CameraController.Listener {

    private val binding by viewBinding(ControllerExchangePartnerBrowserBinding::inflate)

    init {
        retainViewMode = RetainViewMode.RETAIN_DETACH

        registerForActivityResult(BRConstants.REQUEST_IMAGE_RC)
    }

    private val fileSelect = MutableSharedFlow<Uri?>(extraBufferCapacity = 1)
    private val cameraPermission = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    private val cameraPermissionFlow: Flow<Boolean> = cameraPermission
        .onStart {
            val pm = checkNotNull(applicationContext).packageManager
            when {
                !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) -> emit(false)
                hasPermissions(CAMERA_PERMISSIONS) -> emit(true)
                else -> requestPermissions(CAMERA_PERMISSIONS, BRConstants.CAMERA_PERMISSIONS_RC)
            }
        }
        .take(1)
        .flowOn(Dispatchers.Main)

    private val cameraResult = MutableSharedFlow<String?>(extraBufferCapacity = 1)
    private val imageRequestFlow = cameraPermissionFlow
        .flatMapLatest { hasPermissions ->
            if (hasPermissions) {
                pushCameraController()
                cameraResult.take(1)
            } else flowOf(null)
        }
        .flowOn(Dispatchers.Main)

    private val locationPermission = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    override fun onCreateView(view: View) {
        super.onCreateView(view)

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        with(binding) {
            buttonClose.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnCloseClicked(confirmed = false))
            }
            buttonBrowserBack.setOnClickListener {
                webview.goBack()
                if (webview.canGoBack()) {
                    // todo: set button active state
                }
            }
            buttonBrowserForward.setOnClickListener {
                webview.goForward()
                if (webview.canGoForward()) {
                    // todo: set button active state
                }
            }
            buttonBrowserReload.setOnClickListener {
                webview.reload() // todo: confirmation dialog?
            }

            webview.settings.apply {
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                @SuppressLint("SetJavaScriptEnabled")
                javaScriptEnabled = true
            }

            webview.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    if (request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        viewCreatedScope.launch(Dispatchers.Main) {
                            val hasCameraPermission = cameraPermissionFlow.first()
                            if (hasCameraPermission) {
                                request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                            } else {
                                request.deny()
                            }
                        }
                    } else {
                        request.deny()
                    }
                }

                override fun onShowFileChooser(
                    webView: WebView,
                    filePath: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams
                ): Boolean {
                    logInfo("onShowFileChooser")
                    viewCreatedScope.launch(Dispatchers.Main) {
                        val context = checkNotNull(applicationContext)
                        val cameraGranted = cameraPermissionFlow.first()
                        val (intent, file) = createChooserIntent(context, cameraGranted)

                        val selectedFile = if (intent == null) {
                            // No available apps, use internal camera if possible.
                            if (cameraGranted) imageRequestFlow.first()?.toUri() else null
                        } else {
                            startActivityForResult(intent, BRConstants.REQUEST_IMAGE_RC)
                            fileSelect.first()
                        }
                        val result = when {
                            selectedFile != null -> arrayOf(selectedFile)
                            file != null && file.length() > 0 -> arrayOf(file.toUri())
                            else -> emptyArray()
                        }
                        filePath.onReceiveValue(result)
                    }
                    return true
                }
            }

            webview.webViewClient = object : WebViewClient() {
                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    // todo: set browser navigation button active state
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return if (request?.url?.encodedPath.orEmpty().endsWith("/return")) {
                        (currentModel.state as? ExchangeModel.State.ProcessingOrder)?.userAction?.also { action ->
                            eventConsumer.accept(
                                ExchangeEvent.OnBrowserActionCompleted(action.action, false)
                            )
                        }
                        view?.loadUrl("about:blank")
                        true
                    } else {
                        super.shouldOverrideUrlLoading(view, request)
                    }
                }
            }

            (currentModel.state as? ExchangeModel.State.ProcessingOrder)?.userAction?.also { action ->
                labelTitle.text = action.order.provider.name
                webview.loadUrl(action.baseUrl + action.action.url)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        logInfo("onRequestPermissionResult: requestCode: $requestCode")
        when (requestCode) {
            BRConstants.CAMERA_PERMISSIONS_RC -> {
                cameraPermission.tryEmit(grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)
            }
            BRConstants.GEO_REQUEST_ID -> {
                locationPermission.tryEmit(grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.getParcelableExtra(MediaStore.EXTRA_OUTPUT) ?: data?.dataString?.toUri()
        fileSelect.tryEmit(uri)
    }

    override fun onCameraSuccess(file: File) {
        cameraResult.tryEmit(file.absolutePath)
    }

    override fun onCameraClosed() {
        cameraResult.tryEmit(null)
    }

    override fun handleBack(): Boolean {
        return if (binding.webview.canGoBack()) {
            binding.webview.goBack()
            true
        } else {
            super.handleBack()
        }
    }

    override fun ExchangeModel.render() {
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    activity!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun pushCameraController() {
        if (hasPermissions(CAMERA_PERMISSIONS)) {
            router.pushController(
                RouterTransaction.with(CameraController())
                    .pushChangeHandler(FadeChangeHandler())
                    .popChangeHandler(FadeChangeHandler())
            )
        } else {
            requestPermissions(CAMERA_PERMISSIONS, BRConstants.CAMERA_PERMISSIONS_RC)
        }
    }

    private fun createTempImageFile(context: Context) = createTempFile(
        UUID.randomUUID().toString(),
        FILE_SUFFIX,
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    )

    private fun createChooserIntent(
        context: Context,
        cameraGranted: Boolean
    ): Pair<Intent?, File?> {
        val res = checkNotNull(resources)
        val pm = context.packageManager

        val intents = mutableListOf<Intent>()
        val file: File?
        if (cameraGranted) {
            file = createTempImageFile(context)
            val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
                if (resolveActivity(pm) != null) {
                    intents.add(this)
                }
            }
        } else {
            file = null
        }

        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            if (resolveActivity(pm) != null) {
                intents.add(this)
            }
        }

        return if (intents.isEmpty()) {
            null to null
        } else {
            val title = res.getString(R.string.FileChooser_selectImageSource_android)
            Intent(Intent.ACTION_CHOOSER).apply {
                putExtra(Intent.EXTRA_INTENT, intents.last())
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.dropLast(1).toTypedArray())
            } to file
        }
    }

    companion object {
        private val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        private const val FILE_SUFFIX = ".jpg"
    }
}
