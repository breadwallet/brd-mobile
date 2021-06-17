/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 4/21/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.jsbridge

import android.webkit.JavascriptInterface
import com.platform.APIClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class BrdApiJs(
    private val promise: NativePromiseFactory,
    private val apiClient: APIClient
) : JsApi {

    @JvmOverloads
    @JavascriptInterface
    fun request(
        method: String,
        path: String,
        body: String? = null,
        authenticate: Boolean = true
    ) = when (method.toUpperCase(Locale.ROOT)) {
        "GET" -> get(path, authenticate)
        "POST" -> post(path, body, authenticate)
        "PUT" -> put(path, body, authenticate)
        "DELETE" -> delete(path, authenticate)
        else -> promise.create {
            error("Unknown method '$method'.")
        }
    }

    @JvmOverloads
    @JavascriptInterface
    fun get(path: String, authenticate: Boolean = true) = promise.create {
        check(path.isNotBlank()) { "Path cannot be blank." }
        submitRequest(
            Request.Builder()
                .get()
                .url(apiClient.buildUrl(path))
                .build(),
            authenticate
        )
    }

    @JvmOverloads
    @JavascriptInterface
    fun post(path: String, body: String?, authenticate: Boolean = true) = promise.create {
        check(path.isNotBlank()) { "Path cannot be blank." }
        checkNotNull(body) { "Request body required for POST request." }
        submitRequest(
            Request.Builder()
                .post(body.toRequestBody("application/json".toMediaType()))
                .url(apiClient.buildUrl(path))
                .build(),
            authenticate
        )
    }

    @JvmOverloads
    @JavascriptInterface
    fun delete(path: String, authenticate: Boolean = true) = promise.create {
        check(path.isNotBlank()) { "Path cannot be blank." }
        submitRequest(
            Request.Builder()
                .delete()
                .url(apiClient.buildUrl(path))
                .build(),
            authenticate
        )
    }

    @JvmOverloads
    @JavascriptInterface
    fun put(path: String, body: String?, authenticate: Boolean = true) = promise.create {
        check(path.isNotBlank()) { "Path cannot be blank." }
        checkNotNull(body) { "Request body required for PUT request." }
        submitRequest(
            Request.Builder()
                .put(body.toRequestBody("application/json".toMediaType()))
                .url(apiClient.buildUrl(path))
                .build(),
            authenticate
        )
    }

    private fun submitRequest(request: Request, authenticate: Boolean) =
        apiClient.sendRequest(request, authenticate).asJsonObject()

    private fun APIClient.BRResponse.asJsonObject(): JSONObject =
        JSONObject().apply {
            put("isSuccessful", isSuccessful)
            put("status", code)
            put("headers", JSONObject(headers))
            put(
                "body", try {
                    when {
                        bodyText.isBlank() || contentType != "application/json" -> null
                        bodyText.startsWith("[") -> JSONArray(bodyText)
                        bodyText.startsWith("{") -> JSONObject(bodyText)
                        else -> error("Failed to parse response body.")
                    }
                } catch (e: JSONException) {
                    null
                }
            )
        }
}