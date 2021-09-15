/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.exchange

import com.brd.api.brdJson
import com.brd.api.models.CurrencyMethod
import com.brd.api.models.ExchangeCountry
import com.brd.api.models.ExchangeCurrency
import com.brd.api.models.ExchangeOffer
import com.brd.api.models.ExchangeOfferRequest
import com.brd.exchange.ExchangeModel.OfferState
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
class ExchangeModelTests {

    private val usa = brdJson.decodeFromString<ExchangeCountry>(USA_JSON)
    private val uk = brdJson.decodeFromString<ExchangeCountry>(UK_JSON)
    private val countries = listOf(usa, uk)
    private val currencies = brdJson.decodeFromString<Map<String, ExchangeCurrency>>(CURRENCIES_JSON)

    @Test
    fun test_DefaultModel_StateIsInitializing() {
        val initializingState = ExchangeModel.State.Initializing

        assertOnModels {
            assertEquals(initializingState, state)
        }
    }

    @Test
    fun test_DefaultModel_NoOptionsAreSelected() {
        assertOnModels {
            assertNull(selectedCountry)
            assertNull(selectedRegion)
            assertNull(selectedOffer)
            assertNull(selectedPair)
            assertNull(selectedFiatCurrency)
        }
    }

    @Test
    fun test_DefaultModel_LastPurchaseCurrencyIsBtc() {
        assertOnModels {
            assertEquals("btc", lastPurchaseCurrencyCode)
        }
    }

    @Test
    fun test_DefaultModel_InputErrorIsNull() {
        assertOnModels {
            assertNull(inputError)
        }
    }

    @Test
    fun test_DefaultModel_AmountInputsAreEmpty() {
        assertOnModels {
            assertEquals("0", sourceAmountInput)
            assertEquals(0.0, sourceAmount)
            assertNull(formattedSourceAmount)
            assertNull(quoteAmount)
            assertNull(formattedQuoteAmount)
        }
    }

    @Test
    fun test_OfferState_IsIdle_WhenInputsAreEmpty() {
        assertOnModels {
            assertEquals(OfferState.IDLE, offerState)
        }
    }

    @Test
    fun test_OfferState_IsGathering_WhenInputsValidAndOfferRequestNotCreated() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    sourceAmountInput = "10",
                    inputError = null,
                    offerRequest = null,
                )
            }
        ) {
            assertEquals(OfferState.GATHERING, offerState)
        }
    }

    @Test
    fun test_OfferState_IsIdle_WhenInputsInvalidAndOffersNotLoaded() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    sourceAmountInput = "10",
                    inputError = ExchangeModel.InputError.BalanceLow(0.0),
                    offerRequest = null,
                )
            }
        ) {
            assertEquals(OfferState.IDLE, offerState)
        }
    }

    @Test
    fun test_OfferState_IsGathering_WhenOfferRequestStatusIsGathering() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    sourceAmountInput = "10",
                    inputError = null,
                    offerRequest = createOfferRequest(
                        status = ExchangeOfferRequest.Status.GATHERING
                    ),
                )
            }
        ) {
            assertEquals(ExchangeOfferRequest.Status.GATHERING, offerRequest?.status)
            assertEquals(OfferState.GATHERING, offerState)
        }
    }

    @Test
    fun test_OfferState_IsNoOffers_WhenOfferRequestStatusIsCompleteWithNoOffers() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    sourceAmountInput = "10",
                    inputError = null,
                    offerRequest = createOfferRequest(
                        status = ExchangeOfferRequest.Status.COMPLETE,
                        offers = emptyList(),
                    ),
                )
            }
        ) {
            assertEquals(ExchangeOfferRequest.Status.COMPLETE, offerRequest?.status)
            assertEquals(OfferState.NO_OFFERS, offerState)
        }
    }

    @Test
    fun test_OfferState_IsComplete_WhenOfferRequestStatusIsCompleteWithOffers() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    sourceAmountInput = "10",
                    inputError = null,
                    offerRequest = createOfferRequest(
                        status = ExchangeOfferRequest.Status.COMPLETE,
                        offers = listOf(createOffer()),
                    ),
                )
            }
        ) {
            assertEquals(ExchangeOfferRequest.Status.COMPLETE, offerRequest?.status)
            assertEquals(OfferState.COMPLETED, offerState)
        }
    }

    @Test
    fun test_IsRegionConfigured_IsFalse_WhenCountryIsNotConfigured() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    selectedCountry = null,
                    selectedRegion = null,
                    selectedFiatCurrency = null,
                )
            }
        ) {
            assertFalse(isRegionConfigured())
        }
    }

    @Test
    fun test_IsRegionConfigured_IsFalse_WhenCountryIsConfiguredAndRequiredRegionIsNot() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    selectedCountry = usa,
                    selectedRegion = null,
                    selectedFiatCurrency = null,
                )
            }
        ) {
            assertFalse(isRegionConfigured())
        }
    }

    @Test
    fun test_IsRegionConfigured_IsFalse_WhenFiatCurrencyIsNotConfigured() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    selectedCountry = usa,
                    selectedRegion = usa.regions.first(),
                    selectedFiatCurrency = null,
                )
            }
        ) {
            assertFalse(isRegionConfigured())
        }
    }

    @Test
    fun test_IsRegionConfigured_IsTrue_WhenAllDetailsAreConfiguredAndRegionIsNotRequired() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    countries = countries,
                    currencies = currencies,
                    selectedCountry = uk,
                    selectedRegion = null,
                    selectedFiatCurrency = uk.currency,
                )
            }
        ) {
            assertTrue(isRegionConfigured())
        }
    }

    @Test
    fun test_IsRegionConfigured_IsTrue_WhenAllDetailsAreConfigured() {
        assertOnModels(
            mutate = { model ->
                model.copy(
                    countries = countries,
                    currencies = currencies,
                    selectedCountry = usa,
                    selectedRegion = usa.regions.first(),
                    selectedFiatCurrency = usa.currency,
                )
            }
        ) {
            assertTrue(isRegionConfigured())
        }
    }

    @Test
    fun test_EventStringBuilder_ContainsCorrectMode() {
        assertOnModels {
            val subject = event("test")
            when (mode) {
                ExchangeModel.Mode.BUY -> assertEquals("buy.test", subject)
                ExchangeModel.Mode.SELL -> assertEquals("sell.test", subject)
                ExchangeModel.Mode.TRADE -> assertEquals("trade.test", subject)
            }
        }
    }

    @Test
    fun test_ModeBuyCompatibility_OnlySelectsFiatBasePairs() {
        val mode = ExchangeModel.Mode.BUY
        val fiatCurrencies = currencies.values.filter(ExchangeCurrency::isFiat)
        val cryptoCurrencies = currencies.values.filter(ExchangeCurrency::isCrypto)

        assertTrue(fiatCurrencies.isNotEmpty())
        assertTrue(cryptoCurrencies.isNotEmpty())

        fiatCurrencies.forEach { currency ->
            assertTrue(mode.isCompatibleSource(currency))
        }

        cryptoCurrencies.forEach { currency ->
            assertFalse(mode.isCompatibleSource(currency))
        }
    }

    @Test
    fun test_ModeBuyCompatibility_OnlySelectsCryptoQuotePairs() {
        val mode = ExchangeModel.Mode.BUY
        val fiatCurrencies = currencies.values.filter(ExchangeCurrency::isFiat)
        val cryptoCurrencies = currencies.values.filter(ExchangeCurrency::isCrypto)

        assertTrue(fiatCurrencies.isNotEmpty())
        assertTrue(cryptoCurrencies.isNotEmpty())

        fiatCurrencies.forEach { currency ->
            assertFalse(mode.isCompatibleQuote(currency))
        }

        cryptoCurrencies.forEach { currency ->
            assertTrue(mode.isCompatibleQuote(currency))
        }
    }

    @Test
    fun test_ModeTradeCompatibility_OnlySelectsCryptoBasePairs() {
        val mode = ExchangeModel.Mode.TRADE
        val fiatCurrencies = currencies.values.filter(ExchangeCurrency::isFiat)
        val cryptoCurrencies = currencies.values.filter(ExchangeCurrency::isCrypto)

        assertTrue(fiatCurrencies.isNotEmpty())
        assertTrue(cryptoCurrencies.isNotEmpty())

        fiatCurrencies.forEach { currency ->
            assertFalse(mode.isCompatibleSource(currency))
        }

        cryptoCurrencies.forEach { currency ->
            assertTrue(mode.isCompatibleSource(currency))
        }
    }

    @Test
    fun test_ModeTradeCompatibility_OnlySelectsCryptoQuotePairs() {
        val mode = ExchangeModel.Mode.TRADE
        val fiatCurrencies = currencies.values.filter(ExchangeCurrency::isFiat)
        val cryptoCurrencies = currencies.values.filter(ExchangeCurrency::isCrypto)

        assertTrue(fiatCurrencies.isNotEmpty())
        assertTrue(cryptoCurrencies.isNotEmpty())

        fiatCurrencies.forEach { currency ->
            assertFalse(mode.isCompatibleQuote(currency))
        }

        cryptoCurrencies.forEach { currency ->
            assertTrue(mode.isCompatibleQuote(currency))
        }
    }

    @Test
    fun test_ModeSellCompatibility_OnlySelectsCryptoBasePairs() {
        val mode = ExchangeModel.Mode.SELL
        val fiatCurrencies = currencies.values.filter(ExchangeCurrency::isFiat)
        val cryptoCurrencies = currencies.values.filter(ExchangeCurrency::isCrypto)

        assertTrue(fiatCurrencies.isNotEmpty())
        assertTrue(cryptoCurrencies.isNotEmpty())

        fiatCurrencies.forEach { currency ->
            assertFalse(mode.isCompatibleSource(currency))
        }

        cryptoCurrencies.forEach { currency ->
            assertTrue(mode.isCompatibleSource(currency))
        }
    }

    @Test
    fun test_ModeSellCompatibility_OnlySelectsFiatQuotePairs() {
        val mode = ExchangeModel.Mode.SELL
        val fiatCurrencies = currencies.values.filter(ExchangeCurrency::isFiat)
        val cryptoCurrencies = currencies.values.filter(ExchangeCurrency::isCrypto)

        assertTrue(fiatCurrencies.isNotEmpty())
        assertTrue(cryptoCurrencies.isNotEmpty())

        fiatCurrencies.forEach { currency ->
            assertTrue(mode.isCompatibleQuote(currency))
        }

        cryptoCurrencies.forEach { currency ->
            assertFalse(mode.isCompatibleQuote(currency))
        }
    }

    // TODO: ExchangeModel.offerBodyOrNull tests
    // TODO: ExchangeModel.matchesOfferBody tests

    private fun createOfferRequest(
        status: ExchangeOfferRequest.Status,
        createdAt: Instant = Clock.System.now(),
        offers: List<ExchangeOffer> = emptyList(),
    ) = ExchangeOfferRequest(
        url = "",
        createdAt = createdAt,
        status = status,
        countryCode = "",
        regionCode = null,
        sourceCurrencyCode = "",
        quoteCurrencyCode = "",
        offers = offers,
    )

    @OptIn(ExperimentalTime::class)
    private fun createOffer(
        createdAt: Instant = Clock.System.now(),
        expiresAt: Instant = Clock.System.now() + Duration.minutes(10),
    ) = ExchangeOffer(
        offerId = "",
        createdAt = createdAt,
        expiresAt = expiresAt,
        quoteCurrencyMethod = CurrencyMethod.Card(
            status = CurrencyMethod.Status.PENDING,
            message = "",
            description = "",
        ),
        sourceCurrencyMethod = CurrencyMethod.Card(
            status = CurrencyMethod.Status.PENDING,
            message = "",
            description = "",
        ),
        provider = ExchangeOffer.Provider(
            name = "",
            logoUrl = null,
            slug = "",
            url = "",
        ),
        limits = emptyList(),
        invoiceEstimate = null,
    )

    private fun assertOnModels(
        test: Boolean = false,
        mutate: (ExchangeModel) -> ExchangeModel = { it },
        body: ExchangeModel.() -> Unit
    ) {
        modelsInAllModes(test, mutate).forEach(body)
    }

    private fun modelsInAllModes(test: Boolean, mutate: (ExchangeModel) -> ExchangeModel) =
        listOf(
            ExchangeModel.create(mode = ExchangeModel.Mode.BUY, test = test),
            ExchangeModel.create(mode = ExchangeModel.Mode.TRADE, test = test),
            ExchangeModel.create(mode = ExchangeModel.Mode.SELL, test = test),
        ).map(mutate)
}
