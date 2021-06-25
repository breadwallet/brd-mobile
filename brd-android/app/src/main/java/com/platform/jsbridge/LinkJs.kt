/**
 * BreadWallet
 *
 * Created by Ahsan Butt <drew.carlson@breadwallet.com> on 4/20/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.jsbridge

import android.webkit.JavascriptInterface
import com.platform.LinkBus
import com.platform.LinkRequestMessage
import org.json.JSONObject

class LinkJs(
    private val promise: NativePromiseFactory
) : JsApi {

    companion object {
        const val KEY_URL = "url"
    }

    @JvmOverloads
    @JavascriptInterface
    fun openUrl(
        url: String,
        jsonRequest: String? = null
    ) = promise.create {
        LinkBus.sendMessage(LinkRequestMessage(url, jsonRequest))
        JSONObject().apply {
            put(KEY_URL, url)
        }
    }
}