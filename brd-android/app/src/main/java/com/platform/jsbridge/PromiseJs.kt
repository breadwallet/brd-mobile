package com.platform.jsbridge

import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PromiseJs(
    private val webView: WebView,
    private val apiNames: String,
    private val scope: CoroutineScope
) {
    companion object {
        const val JS_NAME = "PromiseJs"
    }

    @JavascriptInterface
    fun injectPromiseHooks() = scope.launch(Dispatchers.Main) {
        webView.evaluateJavascript(
            """
                (function() {
                    let nativeObjs = $apiNames
                    nativeObjs.forEach( nativeObjectName => {
                        let objectName = nativeObjectName.replace("_Native", "")
                        window[objectName] = Object()
                        for (let [key, value] of Object.entries(window[nativeObjectName])) {
                            window[objectName][key] = function() {
                                let primaryArgs = arguments
                                for (var i = 0; i < primaryArgs.length; i++) {
                                  let arg = primaryArgs[i]
                                  if (arg && typeof arg === 'object') {
                                      primaryArgs[i] = JSON.stringify(arg)
                                  }
                                }
            
                                return new Promise(function(resolve, reject) {
                                    let promiseBinder = value.apply(window[nativeObjectName], primaryArgs)
                                    let callbackName = promiseBinder.getJsName()
                                    if (typeof window.brdCallbacks === 'undefined') {
                                        window.brdCallbacks = Object()
                                    }
                                    window.brdCallbacks[callbackName] = {
                                        scope: this,
                                        resolve: resolve,
                                        reject: reject
                                    }
                                    promiseBinder.execute()
                                })
                            }
                        }
                    })
                })()
            """.trimIndent(),
            null
        )
    }
}
