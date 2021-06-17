/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 1/15/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.jsbridge

import android.webkit.JavascriptInterface
import android.webkit.WebView

interface JsApi

class NativeApisJs(
    private val apis: List<JsApi>
) {
    companion object {
        private const val JS_NAME = "NativeApisJs"

        fun with(vararg apis: JsApi) =
            NativeApisJs(apis.toList())
    }

    @JavascriptInterface
    fun getApiNamesJson(): String =
        apis.joinToString(prefix = "[", postfix = "]") {
            "\"${it::class.java.simpleName}_Native\""
        }

    fun attachToWebView(webView: WebView) {
        webView.addJavascriptInterface(PromiseJs(webView, getApiNamesJson()), PromiseJs.JS_NAME)
        apis.forEach { api ->
            val name = "${api::class.java.simpleName}_Native"
            webView.addJavascriptInterface(api, name)
        }
        webView.addJavascriptInterface(this, JS_NAME)
    }
}
