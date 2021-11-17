/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api

import io.ktor.http.*

interface BrdAuthProvider {

    data class Signature(
        val signature: String,
        val timestamp: String,
    )

    var token: String?
    fun hasKey(): Boolean
    fun publicKey(): String
    fun deviceId(): String
    fun sign(method: String, body: String, contentType: String, url: String): Signature
    fun walletId(): String?
    fun clientToken(): String?
    fun authorization(signature: String): String

    fun signUrl(apiHost: BrdApiHost, path: String): String

    abstract class Base : BrdAuthProvider {
        override fun clientToken(): String? = null

        override fun authorization(signature: String): String {
            return "bread $token:$signature"
        }

        override fun signUrl(apiHost: BrdApiHost, path: String): String {
            val (signature, timestamp) = sign("GET", "", "", path)
            return URLBuilder(apiHost.host).apply {
                protocol = URLProtocol.HTTPS
                path(path.trim('/').split('/'))
                parameters["Authorization"] = authorization(signature)
                parameters["Date"] = timestamp
            }.buildString()
        }
    }
}
