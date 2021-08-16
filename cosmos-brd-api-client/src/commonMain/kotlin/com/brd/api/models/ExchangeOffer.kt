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

@Serializable
data class ExchangeOffer(
    @SerialName("offer_id")
    val offerId: String,
    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @SerialName("expires_at")
    @Serializable(with = InstantSerializer::class)
    val expiresAt: Instant,
    @SerialName("quote_currency_method")
    val quoteCurrencyMethod: CurrencyMethod,
    @SerialName("source_currency_method")
    val sourceCurrencyMethod: CurrencyMethod,
    val provider: Provider,
    @SerialName("provider_delivery_estimate")
    val deliveryEstimate: String? = null,
    val limits: List<Limit>,
    @SerialName("invoice_estimate")
    val invoiceEstimate: ExchangeInvoiceEstimate? = null
) {

    val discounts: List<ExchangeInvoiceEstimate.Discount>
        get() = invoiceEstimate?.fees?.flatMap(ExchangeInvoiceEstimate.Fee::discounts).orEmpty()

    @Serializable
    enum class LimitType {
        @SerialName("source_currency_min")
        SOURCE_CURRENCY_MIN,

        @SerialName("source_currency_max")
        SOURCE_CURRENCY_MAX,

        @SerialName("quote_currency_min")
        QUOTE_CURRENCY_MIN,

        @SerialName("quote_currency_max")
        QUOTE_CURRENCY_MAX,
    }

    @Serializable
    data class Provider(
        val name: String = "<Unknown>",
        @SerialName("logo_url")
        val logoUrl: String? = null,
        val slug: String,
        val url: String? = null,
    ) {
        val imageSlug: String
            get() = slug.replace("-test", "")
    }

    @Serializable
    data class Limit(
        val name: String,
        val type: LimitType,
        val amount: String,
        val consumed: String? = null,
        @SerialName("window_start")
        @Serializable(with = InstantSerializer::class)
        val windowStart: Instant?,
        @SerialName("window_end")
        @Serializable(with = InstantSerializer::class)
        val end: Instant?,
    )
}
