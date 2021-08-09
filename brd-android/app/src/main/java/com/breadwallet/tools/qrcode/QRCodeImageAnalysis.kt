/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 4/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.qrcode

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.UseCase
import com.breadwallet.logger.logError
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.Executor

class QRCodeImageAnalysis(
    private val executor: Executor
) : ImageAnalysis.Analyzer {

    private val decodedTextFlow = MutableSharedFlow<String>(replay = 1)

    fun decodedTextFlow(): Flow<String> = decodedTextFlow

    val useCase: UseCase
        get() {
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(executor, this)
            return imageAnalysis
        }

    /**
     * Performs an analysis of the image, searching for the QR code, using the ZXing library.
     */
    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val imageBytes = ByteArray(buffer.remaining())
        buffer[imageBytes]
        val width = image.width
        val height = image.height
        val source = PlanarYUVLuminanceSource(imageBytes, width, height, 0, 0, width, height, false)
        val zxingBinaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val decodedBarcode = QRCodeReader().decode(zxingBinaryBitmap)
            decodedTextFlow.tryEmit(decodedBarcode.text)
        } catch (e: NotFoundException) {
            logError("QR Code decoding error", e)
        } catch (e: ChecksumException) {
            logError("QR Code decoding error", e)
        } catch (e: FormatException) {
            logError("QR Code decoding error", e)
        } finally {
            image.close()
        }
    }
}
