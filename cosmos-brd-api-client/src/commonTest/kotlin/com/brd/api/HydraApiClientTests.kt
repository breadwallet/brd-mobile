/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api

import com.brd.api.models.BrdCurrenciesResult
import com.brd.api.models.ExchangeCountriesResult
import com.brd.api.models.ExchangePairsResult
import kotlin.test.*

@Ignore
class HydraApiClientTests {

    private lateinit var apiClient: BrdApiClient

    private val authProvider: BrdAuthProvider = object : BrdAuthProvider.Base() {
        override var token: String?
            get() = fail()
            set(value) {}
        override fun hasKey(): Boolean = fail()
        override fun publicKey(): String = fail()
        override fun deviceId(): String = fail()
        override fun sign(
            method: String,
            body: String,
            contentType: String,
            url: String
        ): BrdAuthProvider.Signature = fail()

        override fun signUrl(apiHost: BrdApiHost, path: String): String = ""
        override fun walletId(): String? = null
    }
    @BeforeTest
    fun before() {
        apiClient = BrdApiClient.create(BrdApiHost.STAGING, authProvider)
    }

    @Test
    fun testCurrenciesMainnet() = runBlocking {
        val response = apiClient.getCurrencies()

        assertTrue(response is BrdCurrenciesResult.Success)
        assertTrue(response.currencies.isNotEmpty())

        val btc = response.currencies.find { it.code.equals("btc", true) }
        val eth = response.currencies.find { it.code.equals("eth", true) }
        assertEquals(btc?.name, "Bitcoin")
        assertEquals(eth?.name, "Ethereum")
    }

    @Test
    fun testCurrenciesTestnet() = runBlocking {
        val response = apiClient.getCurrencies(mainnet = false)

        assertTrue(response is BrdCurrenciesResult.Success)
        assertTrue(response.currencies.isNotEmpty())

        val btc = response.currencies.find { it.code.equals("btc", true) }
        val eth = response.currencies.find { it.code.equals("eth", true) }
        assertEquals(btc?.currencyId, "bitcoin-testnet:__native__")
        assertEquals(eth?.currencyId, "ethereum-testnet:__native__")
    }

    @Test
    fun testExchangeCountries() = runBlocking {
        val response = apiClient.getExchangeCountries()

        assertTrue(response is ExchangeCountriesResult.Success)
        assertEquals(2, response.detectedCountryCode.length)

        assertNotNull(response.countries.find { (code) -> code == "us" })
    }

    @Test
    fun testExchangePairsSuccess() = runBlocking {
        val response = apiClient.getExchangePairs("us", "ca")

        assertTrue(response is ExchangePairsResult.Success)
        assertTrue(response.supportedPairs.isNotEmpty())

        assertNotNull(response.supportedPairs.find { (fromCode, toCode) ->
            fromCode == "usd" && toCode == "btc"
        })
    }

    @Test
    fun testExchangePairsNotFound() = runBlocking {
        val response1 = apiClient.getExchangePairs("zzzzz", "ca")

        assertTrue(response1 is ExchangePairsResult.Error)
        assertEquals(404, response1.status)

        val response2 = apiClient.getExchangePairs("us", "aaaaa")

        assertTrue(response2 is ExchangePairsResult.Error)
        assertEquals(404, response2.status)

        val response3 = apiClient.getExchangePairs("zzzzz", "aaaaa")

        assertTrue(response3 is ExchangePairsResult.Error)
        assertEquals(404, response3.status)
    }
}
