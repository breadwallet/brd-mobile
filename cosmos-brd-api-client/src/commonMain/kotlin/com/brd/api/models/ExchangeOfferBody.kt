/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ExchangeOfferBody(
    @SerialName("country_code")
    val countryCode: String,
    @SerialName("region_code")
    val regionCode: String?,
    @SerialName("source_currency_code")
    val sourceCurrencyCode: String,
    @SerialName("quote_currency_code")
    val quoteCurrencyCode: String,
    @SerialName("source_currency_amount")
    val sourceCurrencyAmount: Double,
    @Transient
    val currencyId: String = "",
    val test: Boolean = false,
)
