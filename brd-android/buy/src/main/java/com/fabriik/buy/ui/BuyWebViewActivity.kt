package com.fabriik.buy.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.ViewModelProvider
import com.fabriik.buy.R
import com.fabriik.buy.data.Status

class BuyWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var viewModel: BuyWebViewViewModel
    private lateinit var loadingIndicator: ContentLoadingProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_web_view)

        viewModel = ViewModelProvider(this)
            .get(BuyWebViewViewModel::class.java)

        webView = findViewById(R.id.web_view)
        loadingIndicator = findViewById(R.id.loading_bar)

        webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val trimmedUrl = request.url.toString().trimEnd('/')

                if (trimmedUrl.startsWith("file://")) {
                    view.loadUrl(trimmedUrl)
                } else {
                    // Wyre links
                    return false
                }
                return true
            }
        }

        viewModel.getPaymentUrl().observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    loadingIndicator.isVisible = false
                    it.data?.let { response ->
                        webView.loadUrl(
                            response.url
                        )
                    }
                }
                Status.ERROR -> {
                    loadingIndicator.isVisible = false
                    Toast.makeText(
                        this, it.message, Toast.LENGTH_LONG
                    ).show()
                }
                Status.LOADING -> {
                    loadingIndicator.isVisible = true
                }
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        fun getStartIntent(context: Context) = Intent(context, BuyWebViewActivity::class.java)
    }
}