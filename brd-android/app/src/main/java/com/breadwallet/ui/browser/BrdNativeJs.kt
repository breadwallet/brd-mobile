/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/6/2019.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.browser

import android.webkit.JavascriptInterface
import com.breadwallet.tools.util.Utils
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Provides native methods for use in platform-content pages.
 */
object BrdNativeJs {

    const val JS_NAME = "brdNative"
    const val SIGNATURE_HEADER = "x-signature"
    const val DATE_HEADER = "x-date"

    private val sha256 by lazy { MessageDigest.getInstance("SHA-256") }
    private val mac by lazy {
        Mac.getInstance("Hmacsha256").apply {
            val uuid = UUID.randomUUID().toString()
            init(SecretKeySpec(uuid.toByteArray(), "Hmacsha256"))
        }
    }

    @JavascriptInterface
    fun sha256(input: String?): String = when {
        input == null || input.isBlank() -> ""
        else -> sha256.digest(input.toByteArray()).run(Utils::bytesToHex)
    }

    @JavascriptInterface
    fun sign(
        method: String,
        contentDigest: String,
        contentType: String,
        date: String,
        url: String
    ): String = mac.run {
        reset()
        val signingContent = method + contentDigest + contentType + date + url
        doFinal(signingContent.toByteArray()).run(Utils::bytesToHex)
    }
}
