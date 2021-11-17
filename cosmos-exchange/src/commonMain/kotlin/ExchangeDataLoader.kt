/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.exchange

import com.brd.api.BrdApiClient
import com.brd.api.brdJson
import com.brd.api.models.*
import com.brd.concurrent.AtomicReference
import com.brd.concurrent.freeze
import com.brd.logger.Logger
import com.brd.prefs.Preferences
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

private const val CURRENCIES = "exchange-currencies"
private const val COUNTRIES = "exchange-countries"
private const val DETECTED_COUNTRY = "exchange-detected-country"

@OptIn(ExperimentalSerializationApi::class)
class ExchangeDataLoader(
    private val brdApiClient: BrdApiClient,
    private val preferences: Preferences,
) {

    private val scope = CoroutineScope(Default + SupervisorJob())

    private val logger = Logger.create("ExchangeDataLoader")

    private val cachedCountries = AtomicReference<List<ExchangeCountry>?>(null)
    private val cachedCurrencies = AtomicReference<Map<String, ExchangeCurrency>?>(null)
    private val cachedDetectedCountryCode = AtomicReference<String?>(null)
    private val cachedDetectedRegionCode = AtomicReference<String?>(null)

    val countries: List<ExchangeCountry>?
        get() = cachedCountries.value
    val currencies: Map<String, ExchangeCurrency>?
        get() = cachedCurrencies.value
    val detectedCountryCode: String?
        get() = cachedDetectedCountryCode.value
    val detectedRegionCode: String?
        get() = cachedDetectedRegionCode.value

    init {
        freeze()
        fetchData()
    }

    fun fetchData() {
        scope.launch {
            logger.debug("Fetching exchange data")
            if (preferences.contains(COUNTRIES) && countries.isNullOrEmpty()) {
                logger.debug("Found cached countries")
                restoreCountriesFromDisk()

                if (countries.isNullOrEmpty()) {
                    logger.debug("Cached restore failed or missing")
                    fetchCountries()
                }
            } else {
                logger.debug("No cached countries")
                fetchCountries()
            }

            if (preferences.contains(CURRENCIES)) {
                logger.debug("Found cached currencies")
                restoreCurrenciesFromDisk()
            }
        }
    }

    fun clear() {
        logger.debug("Clearing cached data")
        preferences.remove(COUNTRIES)
        preferences.remove(CURRENCIES)
        preferences.remove(DETECTED_COUNTRY)
        emptyMemoryCache()
    }

    fun dispose() {
        logger.debug("Disposing ExchangeDataLoader")
        scope.cancel()
        emptyMemoryCache()
    }

    private fun emptyMemoryCache() {
        cachedCountries.value = null
        cachedCurrencies.value = null
        cachedDetectedCountryCode.value = null
        cachedDetectedRegionCode.value = null
    }

    private suspend fun fetchCountries() {
        logger.debug("Fetching countries")
        when (val result = brdApiClient.getExchangeCountries()) {
            is ExchangeCountriesResult.Success -> {
                cachedCountries.value = result.countries
                cachedDetectedCountryCode.value = result.detectedCountryCode
                cachedDetectedRegionCode.value = result.detectedRegionCode

                scope.launch {
                    val countriesString = brdJson.encodeToString(result.countries)
                    preferences.putString(COUNTRIES, countriesString)
                    preferences.putString(DETECTED_COUNTRY, "${detectedCountryCode}:${detectedRegionCode}")
                }
            }
            is ExchangeCountriesResult.Error -> {
                logger.error("Failed to fetch countries", result)
            }
        }
    }

    private fun restoreCountriesFromDisk() {
        preferences.getStringOrNull(DETECTED_COUNTRY)?.split(":")?.run {
            cachedDetectedCountryCode.value = getOrNull(0)
            cachedDetectedRegionCode.value = getOrNull(1)
        }

        val countries = preferences.getStringOrNull(COUNTRIES)?.let { countriesString ->
            try {
                brdJson.decodeFromString<List<ExchangeCountry>>(countriesString)
            } catch (e: SerializationException) {
                logger.error("Failed to deserialize countries JSON", e)
                null
            }
        }.freeze()
        cachedCountries.value = countries
    }

    private fun restoreCurrenciesFromDisk() {
        val currencies = preferences.getStringOrNull(CURRENCIES)?.let { currenciesString ->
            try {
                brdJson.decodeFromString<Map<String, ExchangeCurrency>>(currenciesString)
            } catch (e: SerializationException) {
                logger.error("Failed to deserialize currencies JSON", e)
                null
            }
        }.freeze()
        cachedCurrencies.value = currencies
    }
}
