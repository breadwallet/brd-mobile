/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api

import com.brd.api.internal.BRDAuthentication
import com.brd.api.models.*
import com.brd.api.models.ExchangeOrder.Action.CryptoReceiveAddress
import com.brd.api.models.ExchangeOrder.Action.CryptoRefundAddress
import com.brd.concurrent.AtomicReference
import io.ktor.client.HttpClient
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.readText
import io.ktor.http.*
import io.ktor.utils.io.charsets.Charsets.UTF_8
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
val brdJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowStructuredMapKeys = true
}

internal class HydraApiClient(
    apiHost: BrdApiHost,
    brdAuthProvider: BrdAuthProvider,
    httpClient: HttpClient = HttpClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BrdApiClient {

    private val apiHost = AtomicReference(apiHost)

    private val http = httpClient.config {
        Json {
            serializer = KotlinxSerializer(brdJson)
            accept(ContentType.Application.Json.withCharset(UTF_8))
        }
        Logging {
            level = LogLevel.HEADERS
            logger = Logger.SIMPLE
        }
        install(BRDAuthentication) {
            brdAuthProvider(brdAuthProvider)
        }
    }

    override var host: BrdApiHost
        get() = apiHost.value
        set(value) {
            apiHost.value = value
        }

    override suspend fun getCurrencies(mainnet: Boolean): BrdCurrenciesResult {
        return withContext(dispatcher) {
            try {
                BrdCurrenciesResult.Success(
                    currencies = http.get("/currencies") {
                        parameter("mainnet", mainnet)
                    }
                )
            } catch (e: ResponseException) {
                BrdCurrenciesResult.Error(
                    status = e.response.status.value,
                    body = e.response.readText()
                )
            }
        }
    }

    override suspend fun getExchangeCountries(): ExchangeCountriesResult {
        return withContext(dispatcher) {
            try {
                http.get<ExchangeCountriesResult.Success>("/exchange/countries")
            } catch (e: ResponseException) {
                ExchangeCountriesResult.Error(
                    status = e.response.status.value,
                    body = e.response.readText()
                )
            }
        }
    }

    override suspend fun getExchangePairs(
        countryCode: String,
        regionCode: String?,
        sourceCurrencyCode: String?,
        quoteCurrencyCode: String?
    ): ExchangePairsResult {
        return withContext(dispatcher) {
            try {
                http.get<ExchangePairsResult.Success>("/exchange/pairs") {
                    parameter("country_code", countryCode)
                    parameter("region_code", regionCode)
                    parameter("source_currency_code", sourceCurrencyCode)
                    parameter("quote_currency_code", quoteCurrencyCode)
                }
            } catch (e: ResponseException) {
                ExchangePairsResult.Error(
                    status = e.response.status.value,
                    body = e.response.readText()
                )
            }
        }
    }

    override suspend fun createOfferRequest(configuration: ExchangeOfferBody): ExchangeOfferRequestResult {
        return withContext(dispatcher) {
            try {
                val offerRequest = http.post<ExchangeOfferRequest>("/exchange/offer-requests") {
                    contentType(ContentType.Application.Json.withCharset(UTF_8))
                    authenticated()
                    body = configuration
                }
                ExchangeOfferRequestResult.Success(offerRequest)
            } catch (e: ResponseException) {
                ExchangeOfferRequestResult.Error(
                    status = e.response.status.value,
                    body = e.response.readText(),
                )
            }
        }
    }

    override suspend fun getOfferRequest(id: String): ExchangeOfferRequest? {
        return withContext(dispatcher) {
            try {
                http.get<ExchangeOfferRequest>("/exchange/offer-requests/$id") {
                    authenticated()
                }
            } catch (e: ResponseException) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun createOrder(offerId: String): ExchangeOrderResult {
        return withContext(dispatcher) {
            try {
                ExchangeOrderResult.Success(
                    order = http.post("/exchange/orders") {
                        contentType(ContentType.Application.Json.withCharset(UTF_8))
                        authenticated()
                        body = buildJsonObject {
                            put("offer_id", offerId)
                        }
                    }
                )
            } catch (e: ResponseException) {
                val bodyString = e.response.readText()
                val jsonData = try {
                    if (e.response.contentType() == ContentType.Application.Json) {
                        brdJson.parseToJsonElement(bodyString)
                    } else null
                } catch (e: SerializationException) {
                    null
                }?.jsonObject
                ExchangeOrderResult.Error(
                    status = e.response.status.value,
                    body = bodyString,
                    message = jsonData?.get("message")?.jsonPrimitive?.contentOrNull,
                    type = jsonData?.get("type")?.jsonPrimitive?.contentOrNull
                        ?.let { brdJson.decodeFromString(it) }
                )
            }
        }
    }

    override suspend fun getOrder(orderId: String): ExchangeOrder? {
        return withContext(dispatcher) {
            try {
                http.get<ExchangeOrder>("/exchange/orders/$orderId") {
                    authenticated()
                }
            } catch (e: ResponseException) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun submitCryptoAddress(action: ExchangeOrder.Action, address: String): Boolean {
        check(action is CryptoReceiveAddress || action is CryptoRefundAddress)
        return withContext(dispatcher) {
            try {
                http.post<Unit>(action.url) {
                    contentType(ContentType.Application.Json.withCharset(UTF_8))
                    authenticated()
                    body = buildJsonObject {
                        put("address", address)
                    }
                }
                true
            } catch (e: ResponseException) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun submitCryptoSendTransactionId(
        action: ExchangeOrder.Action.CryptoSend,
        transactionId: String
    ): Boolean {
        return withContext(dispatcher) {
            try {
                http.post<Unit>(action.url) {
                    contentType(ContentType.Application.Json.withCharset(UTF_8))
                    authenticated()
                    body = buildJsonObject {
                        put("transaction_id", transactionId)
                    }
                }
                true
            } catch (e: ResponseException) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun getMe(): Boolean {
        return withContext(dispatcher) {
            try {
                http.get<String>("/me") {
                    authenticated()
                }
                true
            } catch (e: ResponseException) {
                false
            }
        }
    }

    private fun HttpRequestBuilder.authenticated() {
        header(BRDAuthentication.ENABLE_AUTH_HEADER, "")
    }
}
