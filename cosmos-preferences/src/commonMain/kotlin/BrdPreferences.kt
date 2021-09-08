/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/04/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.prefs

import com.brd.concurrent.freeze
import com.brd.util.CommonLocales
import com.brd.util.currencyCode
import com.brd.util.uuid

internal expect val platformKeyMap: Map<String, String>

class BrdPreferences(
    private val preferences: Preferences,
) {
    companion object {
        internal const val KEY_DEVICE_ID = "cosmos_device_id"
        internal const val KEY_USER_FIAT = "cosmos_user_fiat"

        private const val KEY_COUNTRY_CODE = "cosmos_country_code"
        private const val KEY_REGION_CODE = "cosmos_region_code"
        private const val KEY_LAST_PURCHASE_CURRENCY = "cosmos_last_purchase_currency"
        private const val KEY_LAST_TRADE_SOURCE_CURRENCY = "cosmos_last_trade_source_currency"
        private const val KEY_LAST_TRADE_QUOTE_CURRENCY = "cosmos_last_trade_quote_currency"
        private const val KEY_LAST_ORDER_AMOUNT = "cosmos_last_order_amount"

        private const val KEY_DEBUG_API_HOST = "cosmos_debug_api_host"
        private const val KEY_HYDRA_ACTIVATED = "cosmos_hydra_activated"
        private const val KEY_IS_REWARDS_SET = "cosmos_is_rewards_set"
        private const val KEY_NATIVE_EXCHANGE_UI = "cosmos_native_exchange_ui"
    }

    init {
        freeze()
    }

    var deviceId: String
        get() = preferences.getString(platformKey(KEY_DEVICE_ID), uuid())
        set(value) {
            preferences.putString(platformKey(KEY_DEVICE_ID), value)
        }

    var debugApiHost: String?
        get() = preferences.getStringOrNull(KEY_DEBUG_API_HOST)
        set(value) {
            if (value == null) {
                preferences.remove(KEY_DEBUG_API_HOST)
            } else {
                preferences.putString(KEY_DEBUG_API_HOST, value)
            }
        }

    var hydraActivated: Boolean
        get() = preferences.getBoolean(KEY_HYDRA_ACTIVATED, false)
        set(value) {
            preferences.putBoolean(KEY_HYDRA_ACTIVATED, value)
        }

    var isRewardsAddressSet: Boolean
        get() = preferences.getBoolean(KEY_IS_REWARDS_SET, false)
        set(value) {
            preferences.putBoolean(KEY_IS_REWARDS_SET, value)
        }

    var fiatCurrencyCode: String
        get() = preferences.getString(
            platformKey(KEY_USER_FIAT),
            CommonLocales.current.currencyCode
        )
        set(value) {
            preferences.putString(platformKey(KEY_USER_FIAT), value.lowercase())
        }

    /**
     * The currency code of the user's latest crypto purchase or null.
     */
    var lastPurchaseCurrency: String?
        get() = preferences.getStringOrNull(KEY_LAST_PURCHASE_CURRENCY)
        set(value) {
            if (value == null) {
                preferences.remove(KEY_LAST_PURCHASE_CURRENCY)
            } else {
                preferences.putString(KEY_LAST_PURCHASE_CURRENCY, value.lowercase())
            }
        }

    var lastTradeSourceCurrency: String?
        get() = preferences.getStringOrNull(KEY_LAST_TRADE_SOURCE_CURRENCY)
        set(value) {
            if (value == null) {
                preferences.remove(KEY_LAST_TRADE_SOURCE_CURRENCY)
            } else {
                preferences.putString(KEY_LAST_TRADE_SOURCE_CURRENCY, value.lowercase())
            }
        }

    var lastTradeQuoteCurrency: String?
        get() = preferences.getStringOrNull(KEY_LAST_TRADE_QUOTE_CURRENCY)
        set(value) {
            if (value == null) {
                preferences.remove(KEY_LAST_TRADE_QUOTE_CURRENCY)
            } else {
                preferences.putString(KEY_LAST_TRADE_QUOTE_CURRENCY, value.lowercase())
            }
        }

    var lastOrderAmount: String?
        get() = preferences.getStringOrNull(KEY_LAST_ORDER_AMOUNT)
        set(value) {
            if (value == null) {
                preferences.remove(KEY_LAST_ORDER_AMOUNT)
            } else {
                preferences.putString(KEY_LAST_ORDER_AMOUNT, value)
            }
        }

    var countryCode: String?
        get() = preferences.getStringOrNull(KEY_COUNTRY_CODE)
        set(value) {
            if (value.isNullOrBlank()) {
                preferences.remove(KEY_COUNTRY_CODE)
            } else {
                preferences.putString(KEY_COUNTRY_CODE, value.lowercase())
            }
        }

    var regionCode: String?
        get() = preferences.getStringOrNull(KEY_REGION_CODE)
        set(value) {
            if (value.isNullOrBlank()) {
                preferences.remove(KEY_REGION_CODE)
            } else {
                preferences.putString(KEY_REGION_CODE, value.lowercase())
            }
        }

    var nativeExchangeUI: Boolean
        get() = preferences.getBoolean(KEY_NATIVE_EXCHANGE_UI, false)
        set(value) {
            preferences.putBoolean(KEY_NATIVE_EXCHANGE_UI, value)
        }

    private fun platformKey(key: String): String = platformKeyMap[key] ?: key
}
