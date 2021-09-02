/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 8/21/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform

import com.breadwallet.tools.util.BRConstants
import com.breadwallet.ui.browser.BrdNativeJs
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

/**
 * Build a signed request.
 */
@JvmOverloads
fun buildSignedRequest(
    url: String,
    body: String,
    method: String,
    target: String,
    contentType: String = BRConstants.CONTENT_TYPE_JSON_CHARSET_UTF8
): Request {
    var contentDigest = ""
    if (BRConstants.GET != method) {
        contentDigest = BrdNativeJs.sha256(body)
    }
    val requestDateString = Date().time.toString()

    val signature = BrdNativeJs.sign(
        method,
        contentDigest,
        contentType,
        requestDateString,
        target
    )

    val builder = Request.Builder()
        .url(url)
        .addHeader(BrdNativeJs.SIGNATURE_HEADER, signature)
        .addHeader(BrdNativeJs.DATE_HEADER, requestDateString)
        .header("content-type", contentType)
    when (method) {
        "POST" -> builder.post(body.toRequestBody(null))
        "PUT" -> builder.put(body.toRequestBody(null))
        "PATH" -> builder.patch(body.toRequestBody(null))
    }
    return builder.build()
}
