/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/8/2019.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.middlewares

import android.util.Log
import com.breadwallet.tools.util.Utils
import com.breadwallet.ui.browser.BrdNativeJs
import com.platform.BRHTTPHelper
import com.platform.interfaces.Middleware
import org.eclipse.jetty.server.Request
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/** A base middleware implementation where [handle] is the result of signature validation. */
abstract class SignedRequestMiddleware : Middleware {
    companion object {
        private val TAG = SignedRequestMiddleware::class.java.simpleName
    }

    override fun handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        val requestSignature = request.getHeader(BrdNativeJs.SIGNATURE_HEADER)
        if (requestSignature.isNullOrBlank()) {
            Log.d(TAG, "Request signature is missing.")
            return false
        }

        val requestDateString = request.getHeader(BrdNativeJs.DATE_HEADER)
        if (requestDateString.isNullOrBlank()) {
            Log.e(TAG, "Request date is missing.")
            return false
        }

        val requestDate = requestDateString.toLongOrNull()
        if (requestDate == null) {
            Log.e(TAG, "Request date is invalid.")
            return false
        }

        val timeDifference = Date().time - requestDate
        if (timeDifference !in 0..8000) {
            Log.e(TAG, "Request signature timeout.")
            return false
        }

        var contentType = ""
        var contentDigest = ""
        if ("GET" != request.method) {
            contentType = request.getHeader("content-type")
            if (Utils.isNullOrEmpty(contentType)) {
                contentType = ""
            }

            val body = String(BRHTTPHelper.getBody(request))
            contentDigest = BrdNativeJs.sha256(body)
        }

        val requestUrlAndQuery = when {
            request.queryString.isNullOrBlank() -> request.requestURI
            else -> "${request.requestURI}?${request.queryString}"
        }

        val signature = BrdNativeJs.sign(
                request.method,
                contentDigest,
                contentType,
                requestDateString,
                requestUrlAndQuery
        )

        if (requestSignature != signature) {
            Log.e(TAG, "Request signature does not match.")
            Log.e(TAG, "$requestSignature, $signature")
            return false
        }

        Log.d(TAG, "Request signature accepted.")
        return true
    }
}
