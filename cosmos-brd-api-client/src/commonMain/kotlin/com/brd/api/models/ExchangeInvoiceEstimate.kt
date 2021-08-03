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

@Serializable
data class ExchangeInvoiceEstimate(
    val fees: List<Fee>,
    @SerialName("source_currency")
    val sourceCurrency: Estimate,
    @SerialName("quote_currency")
    val quoteCurrency: Estimate,
) {
    @Serializable
    data class Estimate(
        val subtotal: String,
        val fees: String,
        val total: String,
    )

    @Serializable
    enum class FeeType {
        @SerialName("provider_fee")
        PROVIDER,
        @SerialName("platform_fee")
        PLATFORM,
        @SerialName("network_fee")
        NETWORK
    }

    @Serializable
    enum class DiscountType {
        @SerialName("platform_discount")
        PLATFORM,
        @SerialName("provider_discount")
        PROVIDER,
    }

    @Serializable
    data class Fee(
        val percentage: Float? = null,
        /** Cost of fee before discounts */
        @SerialName("source_currency_amount")
        val sourceCurrencyAmount: String?,
        @SerialName("quote_currency_amount")
        val quoteCurrencyAmount: String?,
        @SerialName("fee_type")
        val type: FeeType,
        val discounts: List<Discount> = emptyList(),
    )

    @Serializable
    data class Discount(
        /** Message to display to the user about their discount, if applicable. */
        val message: String? = null,
        val percentage: Float,
        /** Amount that will be removed from fee. */
        @SerialName("amount")
        val sourceCurrencyAmount: String,
        @SerialName("discount_type")
        val type: DiscountType,
    )
}
