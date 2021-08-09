/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 4/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.jsbridge

import android.webkit.JavascriptInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CameraJs(
    private val promise: NativePromiseFactory,
    private val imageRequestFlow: Flow<String?>
) : JsApi {

    @JavascriptInterface
    fun takePicture() = promise.createForString {
        val url = imageRequestFlow.first()
        check(!url.isNullOrBlank()) {
            "Picture request cancelled."
        }
        url
    }
}
