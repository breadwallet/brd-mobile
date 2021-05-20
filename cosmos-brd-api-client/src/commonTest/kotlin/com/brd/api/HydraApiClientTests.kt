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

import com.brd.api.models.BrdCurrenciesResult
import com.brd.api.models.ExchangeCountriesResult
import com.brd.api.models.ExchangePairsResult
import kotlin.test.*

@Ignore
class HydraApiClientTests {

    private lateinit var apiClient: BrdApiClient

    private val authProvider: BrdAuthProvider = object : BrdAuthProvider {
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
