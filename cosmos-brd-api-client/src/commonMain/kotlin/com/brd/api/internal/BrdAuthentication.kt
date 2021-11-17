/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.internal

import com.brd.api.BrdAuthProvider
import com.brd.logger.Logger
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.charsets.Charsets.UTF_8
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias FetchToken = suspend (HttpClient, String, String?, String, String) -> String?

internal class BrdAuthentication {

    private lateinit var brdAuthProvider: BrdAuthProvider
    private lateinit var fetchToken: FetchToken

    fun brdAuthProvider(brdAuthProvider: BrdAuthProvider) {
        this.brdAuthProvider = brdAuthProvider
    }

    fun fetchToken(fetchToken: FetchToken) {
        this.fetchToken = fetchToken
    }

    companion object : HttpClientFeature<BrdAuthentication, BrdAuthentication> {

        private val authMutex = Mutex()

        private val logger = Logger.create("BRDAuthentication")

        private val AuthenticationPhase = PipelinePhase("Authentication")

        const val ENABLE_AUTH_HEADER = "__brd_auth__"
        const val HEADER_PLATFORM = "X-App-Platform"
        const val HEADER_VERSION = "X-App-Version"

        override val key: AttributeKey<BrdAuthentication> = AttributeKey("BRDAuthentication")

        override fun prepare(block: BrdAuthentication.() -> Unit): BrdAuthentication =
            BrdAuthentication().apply(block)

        override fun install(feature: BrdAuthentication, scope: HttpClient) {
            val brdAuthProvider = feature.brdAuthProvider
            scope.requestPipeline.insertPhaseBefore(HttpRequestPipeline.Render, AuthenticationPhase)
            scope.requestPipeline.intercept(AuthenticationPhase) { body ->
                if (!context.headers.contains(ENABLE_AUTH_HEADER)) return@intercept
                val path = context.url.encodedPath
                logger.debug("Applying authentication for: '$path'")

                val token = brdAuthProvider.token ?: authMutex.withLock {
                    val latestToken = brdAuthProvider.token
                    if (latestToken != null) return@withLock latestToken
                    feature.fetchToken(
                        scope,
                        context.url.clone().path("token").buildString(),
                        brdAuthProvider.clientToken(),
                        brdAuthProvider.deviceId(),
                        brdAuthProvider.publicKey()
                    )
                }

                if (token == null) {
                    logger.error("Failed to restore or fetch token for: '$path'")
                    return@intercept
                } else {
                    brdAuthProvider.token = token
                }
                if (brdAuthProvider.hasKey()) {
                    val content = body as? TextContent
                    context.headers.remove(ENABLE_AUTH_HEADER)

                    val (signature, date) = brdAuthProvider.sign(
                        method = context.method.value,
                        body = content?.text.orEmpty(),
                        contentType = content?.contentType?.withCharset(UTF_8)?.toString().orEmpty(),
                        url = context.url.buildString().substringAfter(context.url.host)
                    )

                    context.header("Date", date)
                    context.header("Authorization", brdAuthProvider.authorization(signature))
                    context.header("X-Wallet-Id", brdAuthProvider.walletId())
                }
            }
        }
    }
}
