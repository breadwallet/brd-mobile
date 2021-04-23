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
    val limits: List<Limit>,
    @SerialName("invoice_estimate")
    val invoiceEstimate: ExchangeInvoiceEstimate? = null
) {

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
    )

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
