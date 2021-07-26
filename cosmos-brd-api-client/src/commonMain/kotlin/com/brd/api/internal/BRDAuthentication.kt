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
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.features.ResponseException
import io.ktor.client.request.*
import io.ktor.content.TextContent
import io.ktor.http.*
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.*
import io.ktor.utils.io.charsets.Charsets.UTF_8
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal class BRDAuthentication {

    private lateinit var brdAuthProvider: BrdAuthProvider

    fun brdAuthProvider(brdAuthProvider: BrdAuthProvider) {
        this.brdAuthProvider = brdAuthProvider
    }

    companion object : HttpClientFeature<BRDAuthentication, BRDAuthentication> {

        private val authMutex = Mutex()

        private val logger = Logger.create("BRDAuthentication")

        private val AuthenticationPhase = PipelinePhase("Authentication")

        const val ENABLE_AUTH_HEADER = "__brd_auth__"

        override val key: AttributeKey<BRDAuthentication> = AttributeKey("BRDAuthentication")

        override fun prepare(block: BRDAuthentication.() -> Unit): BRDAuthentication =
            BRDAuthentication().apply(block)

        private suspend fun fetchToken(url: URLBuilder, scope: HttpClient, brdAuthProvider: BrdAuthProvider): String? {
            return brdAuthProvider.token ?: authMutex.withLock {
                val latestToken = brdAuthProvider.token
                if (latestToken != null) return latestToken

                val requestUrl = url.path("token").build()

                val token = try {
                    logger.debug("Fetching API token")
                    val response = scope.post<JsonObject>(requestUrl) {
                        contentType(ContentType.Application.Json.withCharset(UTF_8))
                        body = buildJsonObject {
                            put("pubKey", brdAuthProvider.publicKey())
                            put("deviceID", brdAuthProvider.deviceId())
                        }
                    }
                    response["token"]?.jsonPrimitive?.contentOrNull
                } catch (e: Throwable) {
                    logger.error("Failed to fetch API token", e)
                    throw e
                }
                brdAuthProvider.token = token
                token
            }
        }

        override fun install(feature: BRDAuthentication, scope: HttpClient) {
            val brdAuthProvider = feature.brdAuthProvider
            scope.requestPipeline.insertPhaseBefore(HttpRequestPipeline.Render, AuthenticationPhase)
            scope.requestPipeline.intercept(AuthenticationPhase) { body ->
                if (!context.headers.contains(ENABLE_AUTH_HEADER)) return@intercept
                val path = context.url.encodedPath
                logger.debug("Applying authentication for: '$path'")
                val token = fetchToken(context.url.clone(), scope, brdAuthProvider)
                if (token == null) {
                    logger.error("Failed to restore or fetch token for: '$path'")
                    return@intercept
                }
                if (brdAuthProvider.hasKey()) {
                    val content = body as? TextContent
                    context.headers.remove(ENABLE_AUTH_HEADER)

                    val (signature, date) = brdAuthProvider.sign(
                        method = context.method.value,
                        body = content?.text ?: "",
                        contentType = content?.contentType?.withCharset(UTF_8)?.toString() ?: "",
                        url = context.url.buildString().substringAfter(context.url.host)
                    )

                    context.header("Date", date)
                    context.header("Authorization", "bread $token:$signature")
                    context.header("X-Wallet-Id", brdAuthProvider.walletId())
                }
            }
        }
    }
}
