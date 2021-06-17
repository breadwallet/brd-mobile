/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 8/6/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.model

import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Price change of a currency over the last 24Hrs.
 */
data class PriceChange (val changePercentage24Hrs: Double,
                        val change24Hrs: Double) {

    private val arrow : String = when {
        change24Hrs > 0 -> "\u25B4"
        change24Hrs < 0 -> "\u25BE"
        else -> ""
    }

    override fun toString(): String {
        val amount = String.format(Locale.getDefault(), "%.2f", change24Hrs.absoluteValue)
        val percentage = String.format(Locale.getDefault(), "%.2f", changePercentage24Hrs.absoluteValue)
        return "$arrow $percentage% ($amount)"
    }

    fun getPercentageChange(): String {
        val percentage = String.format(Locale.getDefault(), "%.2f", changePercentage24Hrs.absoluteValue)
        return "$arrow $percentage%"
    }
}
