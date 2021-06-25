/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.internal

import com.brd.api.BRDAuthProvider
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.features.ResponseException
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.withCharset
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

    private lateinit var brdAuthProvider: BRDAuthProvider

    fun brdAuthProvider(brdAuthProvider: BRDAuthProvider) {
        this.brdAuthProvider = brdAuthProvider
    }

    companion object : HttpClientFeature<BRDAuthentication, BRDAuthentication> {

        private val authMutex = Mutex()

        private val AuthenticationPhase = PipelinePhase("Authentication")

        const val ENABLE_AUTH_HEADER = "__brd_auth__"

        override val key: AttributeKey<BRDAuthentication> = AttributeKey("BRDAuthentication")

        override fun prepare(block: BRDAuthentication.() -> Unit): BRDAuthentication =
            BRDAuthentication().apply(block)

        private suspend fun fetchToken(scope: HttpClient, brdAuthProvider: BRDAuthProvider): String? {
            return brdAuthProvider.token ?: authMutex.withLock {
                val latestToken = brdAuthProvider.token
                if (latestToken != null) return latestToken

                val token = try {
                    val response = scope.post<JsonObject>("/token") {
                        contentType(ContentType.Application.Json.withCharset(UTF_8))
                        body = buildJsonObject {
                            put("pubKey", brdAuthProvider.publicKey())
                            put("deviceID", brdAuthProvider.deviceId())
                        }
                    }
                    response["token"]?.jsonPrimitive?.contentOrNull
                } catch (e: ResponseException) {
                    e.printStackTrace()
                    null
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
                val token = fetchToken(scope, brdAuthProvider) ?: return@intercept
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
                }
            }
        }
    }
}
