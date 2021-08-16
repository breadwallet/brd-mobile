/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api

import com.brd.api.models.*
import io.ktor.client.*

interface BrdApiClient {

    @Suppress("unused")
    companion object {
        fun create(
            host: BrdApiHost,
            authProvider: BrdAuthProvider
        ): BrdApiClient = HydraApiClient(host, authProvider)

        fun create(
            host: BrdApiHost,
            authProvider: BrdAuthProvider,
            httpClient: HttpClient,
        ): BrdApiClient = HydraApiClient(host, authProvider, httpClient)
    }

    var host: BrdApiHost

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

    suspend fun getOfferRequest(id: String): ExchangeOfferRequestResult

    suspend fun createOrder(offerId: String): ExchangeOrderResult

    suspend fun getOrder(orderId: String): ExchangeOrder?

    suspend fun submitCryptoAddress(action: ExchangeOrder.Action, address: String): Boolean

    suspend fun submitCryptoSendTransactionId(action: ExchangeOrder.Action, transactionId: String): Boolean

    suspend fun getMe(): Boolean

    suspend fun setMe(ethereumAddress: String): Boolean

    suspend fun deleteMe(): Boolean

    suspend fun preflight(): Preflight?

    fun signUrl(path: String): String
}
