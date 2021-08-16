/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.util

import com.brd.concurrent.AtomicReference
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

actual class NumberFormatter actual constructor(locale: CommonLocale, currencyFormatter: Boolean) {

    private val cryptoCurrencyCode = AtomicReference<String?>(null)
    private val ref = NSNumberFormatter().apply {
        this.locale = locale
        numberStyle = if (currencyFormatter) {
            NSNumberFormatterCurrencyStyle
        } else {
            numberStyle
        }
    }

    actual var minimumFractionDigits: Int
        get() = ref.minimumFractionDigits.toInt()
        set(value) {
            ref.minimumFractionDigits = value.toULong()
        }

    actual var maximumFractionDigits: Int
        get() = ref.maximumFractionDigits.toInt()
        set(value) {
            ref.maximumFractionDigits = value.toULong()
        }

    actual var currencyCode: String
        get() = ref.currencyCode
        set(value) {
            val newCurrencyCode = value.uppercase()

            if (ref.numberStyle == NSNumberFormatterCurrencyStyle) {
                ref.setCurrencyCode(newCurrencyCode)
            } else {
                cryptoCurrencyCode.value = newCurrencyCode
            }
        }

    actual var currencySymbol: String?
        get() = ref.currencySymbol
        set(value) {
            ref.setCurrencySymbol(currencySymbol)
        }

    actual var alwaysShowDecimalSeparator: Boolean
        get() = ref.alwaysShowsDecimalSeparator
        set(value) {
            ref.alwaysShowsDecimalSeparator = value
        }

    actual fun format(double: Double): String {
        val currencyCodeOverride = cryptoCurrencyCode.value
        val formattedString = ref.stringFromNumber(NSNumber(double)) ?: ""
        return if (currencyCodeOverride == null) {
            formattedString
        } else {
            "$formattedString $currencyCodeOverride"
        }
    }
}
