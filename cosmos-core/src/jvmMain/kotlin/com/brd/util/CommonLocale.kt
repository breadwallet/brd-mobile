/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.util

import java.text.NumberFormat
import java.util.*

actual typealias CommonLocale = Locale

actual val CommonLocale.currencyCode: String
    get() = Currency.getInstance(this).currencyCode

actual val CommonLocale.countryCode: String
    get() = country

actual object CommonLocales {

    actual val root: CommonLocale
        get() = Locale.ROOT

    actual val current: CommonLocale
        get() = Locale.getDefault()

    actual fun forCountryCode(countryCode: String): CommonLocale {
        return checkNotNull(
            Locale.getAvailableLocales().firstOrNull {
                it.country.equals(countryCode, true )
            }
        ) { "Locale for '$countryCode' not found." }
    }

    actual fun forId(localeId: String): CommonLocale {
        return Locale.forLanguageTag(localeId.replace('_', '-'))
    }

    actual fun allIds(): List<String> {
        return Locale.getAvailableLocales().map(Locale::toString)
    }
}
