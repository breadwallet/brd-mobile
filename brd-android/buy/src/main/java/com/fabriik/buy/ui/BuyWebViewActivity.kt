package com.fabriik.buy.ui

import androidx.appcompat.app.AppCompatActivity

class BuyWebViewActivity : AppCompatActivity() {

    private val viewModel by viewModels<BuyWebViewViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBuyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.settings.apply {
            domStorageEnabled = true
            javaScriptEnabled = true
        }

        binding.webView.webViewClient = object : WebViewClient() {
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
                    binding.loadingBar.isVisible = false
                    it.data?.let { response ->
                        binding.webView.loadUrl(
                            response.url
                        )
                    }
                }
                Status.ERROR -> {
                    binding.loadingBar.isVisible = false
                    Toast.makeText(
                        this, it.message, Toast.LENGTH_LONG
                    ).show()
                }
                Status.LOADING -> {
                    binding.loadingBar.isVisible = true
                }
            }
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}