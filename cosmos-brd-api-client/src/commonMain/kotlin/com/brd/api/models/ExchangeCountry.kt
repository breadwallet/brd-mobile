/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

import kotlinx.serialization.Serializable

/**
 * A country that supports BRD exchange services.
 */
@Serializable
data class ExchangeCountry(
    /** The two-letter country code. (example "US") */
    val code: String,
    /** The full country name. */
    val name: String,
    /** The primary currency for. */
    val currency: ExchangeCurrency,
    /** Possible region options for this country. */
    val regions: List<ExchangeRegion>,
) {
    override fun toString(): String {
        return "ExchangeCountry(" +
                "code='$code', " +
                "name='$name', " +
                "currency=$currency, " +
                "regions=${regions.size})"
    }
}
