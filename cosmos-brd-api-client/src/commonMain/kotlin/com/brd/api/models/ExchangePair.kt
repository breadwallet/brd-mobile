/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

@Serializable
data class ExchangePair(
    val fromCode: String,
    val toCode: String,
    val rate: Double,
    val timestamp: Instant,
) {
    fun estimatedOutput(sourceAmount: Double): Double? {
        if (sourceAmount == 0.0) return null
        return (sourceAmount / rate).absoluteValue.let { result ->
            if (result.isInfinite() || result.isNaN()) null else result
        }
    }

    fun inputFromOutput(quoteAmount: Double): Double? {
        if (quoteAmount == 0.0) return null
        return (quoteAmount * rate).absoluteValue.let { result ->
            if (result.isInfinite() || result.isNaN()) null else result
        }
    }
}
