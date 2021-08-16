/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.util

import platform.Foundation.*

actual typealias CommonLocale = NSLocale

actual val CommonLocale.currencyCode: String
    get() = checkNotNull(currencyCode())

actual val CommonLocale.countryCode: String
    get() = countryCode() ?: ""

actual object CommonLocales {

    actual val root: CommonLocale
        get() = NSLocale.systemLocale()

    actual val current: CommonLocale
        get() = NSLocale.currentLocale

    actual fun forCountryCode(countryCode: String): CommonLocale {
        val localeIds = NSLocale.availableLocaleIdentifiers
            .filter { (it as String).endsWith(countryCode, true) }
        return NSLocale(checkNotNull(localeIds.last() as? String) {
            "Locale for '$countryCode' not found."
        })
    }

    actual fun forId(localeId: String): CommonLocale = NSLocale(localeId)

    @Suppress("UNCHECKED_CAST")
    actual fun allIds(): List<String> {
        return NSLocale.availableLocaleIdentifiers as List<String>
    }
}
