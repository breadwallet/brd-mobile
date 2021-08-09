/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.BuildConfig
import com.breadwallet.databinding.ControllerExchangePartnerBrowserBinding

class PartnerBrowserController(args: Bundle? = null) : ExchangeController.ChildController(args) {

    private val binding by viewBinding(ControllerExchangePartnerBrowserBinding::inflate)

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

            webview.webChromeClient = object : WebChromeClient() {
            }
            webview.settings.apply {
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                @SuppressLint("SetJavaScriptEnabled")
                javaScriptEnabled = true
            }
            webview.webViewClient = object : WebViewClient() {
                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    // todo: set browser navigation button active state
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return if (request?.url?.encodedPath.orEmpty().endsWith("/return")) {
                        (currentModel.state as? ExchangeModel.State.ProcessingOrder)?.userAction?.also { action ->
                            eventConsumer.accept(
                                ExchangeEvent.OnBrowserActionCompleted(action.action, cancelled = false)
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
}
