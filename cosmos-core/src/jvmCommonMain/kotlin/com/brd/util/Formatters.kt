/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.util

import java.text.DecimalFormat
import java.util.*

actual class NumberFormatter actual constructor(locale: CommonLocale, currencyFormatter: Boolean) {

    private val ref = if (currencyFormatter) {
        DecimalFormat.getCurrencyInstance(locale)
    } else {
        DecimalFormat.getInstance(locale)
    } as DecimalFormat

    actual var minimumFractionDigits: Int
        get() = ref.minimumFractionDigits
        set(value) {
            ref.minimumFractionDigits = value
        }

    actual var maximumFractionDigits: Int
        get() = ref.maximumFractionDigits
        set(value) {
            ref.maximumFractionDigits = value
        }

    actual var currencyCode: String
        get() = ref.currency.currencyCode
        set(value) {
            val selectedCurrency = Currency.getAvailableCurrencies()
                .find { it.currencyCode.equals(value, true) }

            if (selectedCurrency == null) {
                val escapedCurrencyCode = "'${value.uppercase()}'"
                ref.applyPattern("#,##0.######## $escapedCurrencyCode")
            } else {
                ref.currency = selectedCurrency
            }
        }

    actual var currencySymbol: String?
        get() = ref.currency.symbol
        set(value) = Unit // impossible to change

    actual var alwaysShowDecimalSeparator: Boolean
        get() = ref.isDecimalSeparatorAlwaysShown
        set(value) {
            ref.isDecimalSeparatorAlwaysShown = value
        }

    actual fun format(double: Double): String {
        return ref.format(double)
    }
}
