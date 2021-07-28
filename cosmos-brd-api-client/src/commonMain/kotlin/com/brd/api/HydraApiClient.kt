/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api

import com.brd.api.internal.BrdAuthentication
import com.brd.api.models.*
import com.brd.api.models.ExchangeOrder.Action.Type.CRYPTO_RECEIVE_ADDRESS
import com.brd.api.models.ExchangeOrder.Action.Type.CRYPTO_REFUND_ADDRESS
import com.brd.concurrent.AtomicReference
import com.brd.concurrent.freeze
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
    useAlternativeNames = false
}

internal class HydraApiClient(
    apiHost: BrdApiHost,
    private val brdAuthProvider: BrdAuthProvider,
    httpClient: HttpClient = HttpClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BrdApiClient {

    @Suppress("JoinDeclarationAndAssignment")
    private val apiHost: AtomicReference<BrdApiHost>

    // NOTE: Create in init to avoid freezing during
    // construction, before initialization is completed.
    @Suppress("JoinDeclarationAndAssignment")
    private val http: HttpClient

    init {
        val brdAuthProvider = brdAuthProvider
        http = httpClient.config {
            Json {
                serializer = KotlinxSerializer(brdJson)
                accept(ContentType.Application.Json.withCharset(UTF_8))
            }
            Logging {
                level = LogLevel.HEADERS
                logger = Logger.SIMPLE
            }
            install(BrdAuthentication) {
                brdAuthProvider(brdAuthProvider)
            }
        }
        this.apiHost = AtomicReference(apiHost)
        freeze()
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
                    currencies = http.get(urlFor("currencies")) {
                        parameter("mainnet", mainnet)
                    }
                )
            } catch (e: Throwable) {
                val response = (e as? ResponseException)?.response
                BrdCurrenciesResult.Error(
                    status = response?.status?.value ?: 0,
                    body = response?.readText() ?: e.message.orEmpty(),
                )
            }
        }
    }

    override suspend fun getExchangeCountries(): ExchangeCountriesResult {
        return withContext(dispatcher) {
            try {
                http.get<ExchangeCountriesResult.Success>(urlFor("exchange", "countries"))
            } catch (e: Throwable) {
                val response = (e as? ResponseException)?.response
                ExchangeCountriesResult.Error(
                    status = response?.status?.value ?: 0,
                    body = response?.readText() ?: e.message.orEmpty(),
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
                http.get<ExchangePairsResult.Success>(urlFor("exchange", "pairs")) {
                    parameter("country_code", countryCode)
                    parameter("region_code", regionCode)
                    parameter("source_currency_code", sourceCurrencyCode)
                    parameter("quote_currency_code", quoteCurrencyCode)
                }
            } catch (e: Throwable) {
                val response = (e as? ResponseException)?.response
                ExchangePairsResult.Error(
                    status = response?.status?.value ?: 0,
                    body = response?.readText() ?: e.message.orEmpty()
                )
            }
        }
    }

    override suspend fun createOfferRequest(configuration: ExchangeOfferBody): ExchangeOfferRequestResult {
        return withContext(dispatcher) {
            try {
                val offerRequest = http.post<ExchangeOfferRequest>(urlFor("exchange", "offer-requests")) {
                    contentType(ContentType.Application.Json.withCharset(UTF_8))
                    authenticated()
                    body = configuration
                }
                ExchangeOfferRequestResult.Success(offerRequest)
            } catch (e: Throwable) {
                val response = (e as? ResponseException)?.response
                ExchangeOfferRequestResult.Error(
                    status = response?.status?.value ?: 0,
                    body = response?.readText() ?: e.message.orEmpty(),
                )
            }
        }
    }

    override suspend fun getOfferRequest(id: String): ExchangeOfferRequestResult {
        return withContext(dispatcher) {
            try {
                ExchangeOfferRequestResult.Success(
                    http.get(urlFor("exchange", "offer-requests", id)) {
                        authenticated()
                    }
                )
            } catch (e: Throwable) {
                val response = (e as? ResponseException)?.response
                ExchangeOfferRequestResult.Error(
                    status = response?.status?.value ?: 0,
                    body = response?.readText() ?: e.message.orEmpty(),
                )
            }
        }
    }

    override suspend fun createOrder(offerId: String): ExchangeOrderResult {
        return withContext(dispatcher) {
            try {
                ExchangeOrderResult.Success(
                    order = http.post(urlFor("exchange", "orders")) {
                        contentType(ContentType.Application.Json.withCharset(UTF_8))
                        authenticated()
                        body = buildJsonObject {
                            put("offer_id", offerId)
                        }
                    }
                )
            } catch (e: Throwable) {
                val response = (e as? ResponseException)?.response
                val bodyString = response?.readText()
                val jsonData = try {
                    if (response?.contentType() == ContentType.Application.Json && !bodyString.isNullOrBlank()) {
                        brdJson.parseToJsonElement(bodyString)
                    } else null
                } catch (e: SerializationException) {
                    null
                }?.jsonObject
                ExchangeOrderResult.Error(
                    status = response?.status?.value ?: 0,
                    body = bodyString ?: e.message.orEmpty(),
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
                http.get<ExchangeOrder>(urlFor("exchange", "orders", orderId)) {
                    authenticated()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun submitCryptoAddress(action: ExchangeOrder.Action, address: String): Boolean {
        check(action.type == CRYPTO_RECEIVE_ADDRESS || action.type == CRYPTO_REFUND_ADDRESS)
        return withContext(dispatcher) {
            try {
                http.post<Unit>(urlFor(action.url)) {
                    contentType(ContentType.Application.Json.withCharset(UTF_8))
                    authenticated()
                    body = buildJsonObject {
                        put("address", address)
                    }
                }
                true
            } catch (e: Throwable) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun submitCryptoSendTransactionId(
        action: ExchangeOrder.Action,
        transactionId: String
    ): Boolean {
        check(action.type == ExchangeOrder.Action.Type.CRYPTO_SEND)
        return withContext(dispatcher) {
            try {
                http.post<Unit>(urlFor(action.url)) {
                    contentType(ContentType.Application.Json.withCharset(UTF_8))
                    authenticated()
                    body = buildJsonObject {
                        put("transaction_id", transactionId)
                    }
                }
                true
            } catch (e: Throwable) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun getMe(): Boolean {
        return withContext(dispatcher) {
            try {
                http.get<String>(urlFor("me")) {
                    authenticated()
                }
                true
            } catch (e: Throwable) {
                false
            }
        }
    }

    override suspend fun setMe(ethereumAddress: String): Boolean {
        return withContext(dispatcher) {
            try {
                http.put<Unit>(urlFor("me")) {
                    contentType(ContentType.Application.Json.withCharset(UTF_8))
                    authenticated()
                    body = buildJsonObject {
                        put("ethereum_address", ethereumAddress)
                    }
                }
                true
            } catch (e: Throwable) {
                false
            }
        }
    }

    override suspend fun deleteMe(): Boolean {
        return withContext(dispatcher) {
            try {
                http.delete<Unit>(urlFor("me"))
                true
            } catch (e: Throwable) {
                false
            }
        }
    }

    override suspend fun preflight(publicKey: String): Preflight? {
        return withContext(dispatcher) {
            try {
                http.get(urlFor("me", "preflight", publicKey)) {
                    authenticated()
                }
            } catch (e: Throwable) {
                null
            }
        }
    }

    override fun signUrl(path: String): String {
        return brdAuthProvider.signUrl(apiHost.value, path)
    }

    private fun HttpRequestBuilder.authenticated() {
        header(BrdAuthentication.ENABLE_AUTH_HEADER, "")
    }

    private fun urlFor(vararg pathComponents: String): String {
        val (host) = host
        val path = pathComponents.joinToString("/").trimStart('/')
        return "$host/$path"
    }
}
