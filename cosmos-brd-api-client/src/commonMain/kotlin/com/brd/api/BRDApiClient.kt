/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brd.api

import com.brd.api.models.*
import io.ktor.client.HttpClient

private const val STAGING_API_HOST = "brd-web-staging.com"//"stage2.breadwallet.com"
private const val PRODUCTION_API_HOST = "api.breadwallet.com"

interface BRDAuthProvider {
    data class Signature(
        val signature: String,
        val timestamp: String,
    )

    var token: String?
    fun hasKey(): Boolean
    fun publicKey(): String
    fun deviceId(): String
    fun sign(method: String, body: String, contentType: String, url: String): Signature
}

interface BRDApiClient {

    @Suppress("unused")
    companion object {
        fun create(authProvider: BRDAuthProvider): BRDApiClient =
            HydraApiClient(
                STAGING_API_HOST,
                brdAuthProvider = authProvider
            )

        fun create(authProvider: BRDAuthProvider, httpClient: HttpClient): BRDApiClient =
            HydraApiClient(STAGING_API_HOST, httpClient, authProvider)

        fun create(authProvider: BRDAuthProvider, host: String): BRDApiClient =
            HydraApiClient(host, brdAuthProvider = authProvider)

        fun create(authProvider: BRDAuthProvider, host: String, httpClient: HttpClient): BRDApiClient =
            HydraApiClient(host, httpClient, authProvider)
    }

    /**
     * Fetch a list of supported mainnet [BrdCurrency]s or testnet
     * currencies when [mainnet] is false.
     */
    suspend fun getCurrencies(mainnet: Boolean = true): BrdCurrenciesResult

    /**
     * Fetch a list of supported [com.brd.api.models.ExchangeCountry]s for
     * use with other exchange APIs.
     *
     * @see getExchangePairs
     */
    suspend fun getExchangeCountries(): ExchangeCountriesResult

    /**
     * Fetch a list of supported [com.brd.api.models.ExchangePair]s for the
     * given [countryCode] and [regionCode].
     */
    suspend fun getExchangePairs(
        countryCode: String,
        regionCode: String? = null,
        sourceCurrencyCode: String? = null,
        quoteCurrencyCode: String? = null,
    ): ExchangePairsResult

    suspend fun createOfferRequest(configuration: ExchangeOfferBody): ExchangeOfferRequestResult

    suspend fun getOfferRequest(id: String): ExchangeOfferRequest?

    suspend fun createOrder(offerId: String): ExchangeOrderResult

    suspend fun getOrder(orderId: String): ExchangeOrder?

    suspend fun submitCryptoAddress(action: ExchangeOrder.Action, address: String): Boolean

    suspend fun submitCryptoSendTransactionId(action: ExchangeOrder.Action.CryptoSend, transactionId: String): Boolean

    suspend fun getMe(): Boolean
}
