/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 4/4/21.
 * Copyright (c) 2021 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.breadwallet.tools.qrcode

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import androidx.camera.core.ImageProxy
import com.breadwallet.logger.logError
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.Executor

class QRCodeImageAnalysis(
    private val executor: Executor
) : ImageAnalysis.Analyzer {

    private val decodedTextFlow = MutableSharedFlow<String>(replay = 1)

    fun decodedTextFlow() : Flow<String> = decodedTextFlow

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
           logError( "QR Code decoding error", e)
        } catch (e: ChecksumException) {
            logError("QR Code decoding error", e)
        } catch (e: FormatException) {
            logError("QR Code decoding error", e)
        } finally {
            image.close()
        }
    }
}