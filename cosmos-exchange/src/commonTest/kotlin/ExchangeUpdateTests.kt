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
import com.brd.api.models.ExchangeCountriesResult
import com.brd.api.models.ExchangeCountry
import com.brd.api.models.ExchangeCurrency
import kotlinx.serialization.decodeFromString
import kt.mobius.Next
import kotlin.test.*

class ExchangeUpdateTests {

    private val usa = brdJson.decodeFromString<ExchangeCountry>(USA_JSON)
    private val uk = brdJson.decodeFromString<ExchangeCountry>(UK_JSON)
    private val countries = listOf(usa, uk)
    private val currencies = brdJson.decodeFromString<Map<String, ExchangeCurrency>>(CURRENCIES_JSON)

    @BeforeTest
    fun before() {

    }

    @Test
    fun test_OnCountriesLoaded_StateRemainsInitializingAndUserPrefsAreLoaded() {
        val initialModel = ExchangeModel.create(ExchangeModel.Mode.BUY, test = true)
        val defaultCountry = "us"
        val defaultRegion = "ca"
        val event = ExchangeEvent.OnCountriesLoaded(listOf(usa), defaultCountry, defaultRegion)
        val next = ExchangeUpdate(initialModel, event)

        assertTrue(next.hasModel())
        assertTrue(next.hasEffects())
        assertEquals(1, next.effects().size)
        next.assertHasEffect(ExchangeEffect.LoadUserPreferences)

        val model = next.modelUnsafe()

        assertEquals(ExchangeModel.State.Initializing, model.state)
        assertEquals(usa.code, model.countries.first().code)
        assertEquals(defaultCountry, model.selectedCountry?.code)
        assertEquals(defaultRegion, model.selectedRegion?.code)
    }

    @Test
    fun test_OnCountriesError_SetsNetworkErrorState() {
        val initialModel = ExchangeModel.create(ExchangeModel.Mode.BUY, test = true)

        val event = ExchangeEvent.OnCountriesError(ExchangeCountriesResult.Error(status = 0, body = ""))
        val next = ExchangeUpdate(initialModel, event)

        assertTrue(next.hasModel())
        assertFalse(next.hasEffects())

        val model = next.modelUnsafe()

        val errorState = ExchangeModel.ErrorState(
            message = null,
            title = null,
            type = ExchangeModel.ErrorState.Type.NetworkError,
            isRecoverable = true,
            debugMessage = "0 : "
        )

        assertEquals(errorState, model.errorState)
    }
}

fun <M, F> Next<M, F>.assertHasEffect(expected: F) {
    assertTrue(effects().contains(expected), "Expected effect was missing: $expected")
}
