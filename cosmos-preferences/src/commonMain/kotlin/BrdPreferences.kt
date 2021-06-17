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
        private const val KEY_PREVIOUSLY_PURCHASED_CURRENCY =
            "cosmos_previously_purchased_currency"
    }

    init {
        freeze()
    }

    var deviceId: String
        get() = preferences.getString(platformKey(KEY_DEVICE_ID), uuid())
        set(value) {
            preferences.putString(platformKey(KEY_DEVICE_ID), value)
        }

    var fiatCurrencyCode: String
        get() = preferences.getString(platformKey(KEY_USER_FIAT))
        set(value) {
            preferences.putString(platformKey(KEY_USER_FIAT), value)
        }

    /**
     * The currency code of the user's latest crypto purchase or "btc".
     */
    var previouslyPurchasedCurrency: String
        get() = preferences.getString(KEY_PREVIOUSLY_PURCHASED_CURRENCY, "btc")
        set(value) {
            preferences.putString(KEY_PREVIOUSLY_PURCHASED_CURRENCY, value)
        }

    var countryCode: String?
        get() = preferences.getStringOrNull(KEY_COUNTRY_CODE)
        set(value) {
            if (value.isNullOrBlank()) {
                preferences.remove(KEY_COUNTRY_CODE)
            } else {
                preferences.putString(KEY_COUNTRY_CODE, value)
            }
        }

    var regionCode: String?
        get() = preferences.getStringOrNull(KEY_REGION_CODE)
        set(value) {
            if (value.isNullOrBlank()) {
                preferences.remove(KEY_REGION_CODE)
            } else {
                preferences.putString(KEY_REGION_CODE, value)
            }
        }

    private fun platformKey(key: String): String = platformKeyMap[key] ?: key
}
