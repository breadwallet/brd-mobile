/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/2/21.
 * Copyright (c) 2021 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
        // val discounts: List<Discount> = emptyList(),
    )

    @Serializable
    data class Discount(
        /** Message to display to the user about their discount, if applicable. */
        val message: String? = null,
        val percentage: Float,
        /** Amount that will be removed from fee. */
        @SerialName("source_currency_amount")
        val sourceCurrencyAmount: String,
        val type: DiscountType,
    )
}
