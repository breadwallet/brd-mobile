/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.bakerapi

import com.brd.bakerapi.models.BakerResult
import com.brd.bakerapi.models.BakersResult
import com.brd.bakerapi.models.PayoutAccuracy
import com.brd.bakerapi.models.PayoutTiming
import com.brd.bakerapi.models.ServiceHealth
import io.ktor.client.HttpClient
import io.ktor.client.features.ResponseException
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.readText
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
private val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
}

private const val API_HOST = "api.baking-bad.org"

internal class BakingBadApiClient(
    apiHost: String = API_HOST,
    httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BakersApiClient {

    private val http = httpClient.config {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = apiHost
            }
        }
    }

    override suspend fun getBakers(): BakersResult {
        return withContext(dispatcher) {
            try {
                BakersResult.Success(
                    bakers = http.get("/v2/bakers") {
                        parameter("accuracy", PayoutAccuracy.PRECISE)
                        parameter("timing", listOf(PayoutTiming.STABLE, PayoutTiming.UNSTABLE).joinToString(","))
                        parameter("health", ServiceHealth.ACTIVE)
                    }
                )
            } catch (e: ResponseException) {
                BakersResult.Error.HttpError(
                    e.response.status.value,
                    e.response.readText()
                )
            } catch (e: SerializationException) {
                BakersResult.Error.ResponseError(
                    e.message ?: e.stackTraceToString()
                )
            }
        }
    }

    override suspend fun getBaker(address: String): BakerResult {
        return withContext(dispatcher) {
            try {
                BakerResult.Success(
                    baker = http.get("/v2/bakers/$address")
                )
            } catch (e: ResponseException) {
                BakerResult.Error.HttpError(
                    e.response.status.value,
                    e.response.readText()
                )
            } catch (e: SerializationException) {
                BakerResult.Error.ResponseError(
                    e.message ?: e.stackTraceToString()
                )
            }
        }
    }
}