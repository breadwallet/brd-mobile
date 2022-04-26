/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 7/19/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.web

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import com.bluelinelabs.conductor.asTransaction
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import com.brd.exchange.ExchangeModel
import com.breadwallet.BuildConfig
import com.breadwallet.R
import com.breadwallet.databinding.ControllerExchangePartnerBrowserBinding
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.exchange.ExchangeController
import com.breadwallet.ui.home.HomeController

private const val ARG_URL = "BrowserController.URL"
private const val ARG_TITLE = "BrowserController.TITLE"

private const val TRADE_PATH = "/native/trade"

class BrowserController(args: Bundle? = null) : BaseController(args) {

    constructor(url: String, title: String? = null) : this(
        bundleOf(
            ARG_URL to url,
            ARG_TITLE to title,
        )
    )

    override val layoutId: Int = R.layout.controller_exchange_partner_browser

    private val binding by viewBinding(ControllerExchangePartnerBrowserBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        with(binding) {
            browserControls.visibility = View.GONE
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
            webview.settings.apply {
                @SuppressLint("SetJavaScriptEnabled")
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
            }
            webview.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest
                ): Boolean {
                    return when {
                        request.url.path == TRADE_PATH -> {
                            goToTrade()
                            true
                        }
                        request.url.toString().startsWith("mailto:") -> {
                            createEmail(request.url.toString())
                            true
                        }
                        else -> super.shouldOverrideUrlLoading(view, request)
                    }
                }
            }

            webview.loadUrl(arg(ARG_URL))
            buttonBrowserBack.setOnClickListener {
                webview.goBack()
            }
            buttonBrowserForward.setOnClickListener {
                webview.goForward()
            }
            buttonBrowserReload.setOnClickListener {
                webview.reload()
            }
            buttonClose.setOnClickListener {
                router.popController(this@BrowserController)
            }
            labelTitle.text = argOptional(ARG_TITLE)
        }
    }

    private fun goToTrade() {
        val home = router.backstack.first { it.controller is HomeController }
        router.setBackstack(
            listOf(
                home,
                ExchangeController(mode = ExchangeModel.Mode.TRADE).asTransaction(
                    VerticalChangeHandler(),
                    VerticalChangeHandler(),
                )
            ),
            VerticalChangeHandler()
        )
    }

    private fun createEmail(mailToString: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse(mailToString)
        activity?.startActivity(intent)
    }
}
