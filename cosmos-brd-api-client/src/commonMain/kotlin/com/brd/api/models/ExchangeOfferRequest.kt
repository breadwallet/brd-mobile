/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

import com.brd.api.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ExchangeOfferRequest(
    val url: String,
    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    val status: Status,
    @SerialName("country_code")
    val countryCode: String,
    @SerialName("region_code")
    val regionCode: String?,
    @SerialName("source_currency_code")
    val sourceCurrencyCode: String,
    @SerialName("quote_currency_code")
    val quoteCurrencyCode: String,
    val offers: List<ExchangeOffer>,
) {
    @Transient
    val id: String = url.substringAfterLast("/")

    @Serializable
    enum class Status {
        /** Finding offers. */
        @SerialName("gathering")
        GATHERING,
        /** All Known Offers are Included */
        @SerialName("complete")
        COMPLETE,
    }
}
