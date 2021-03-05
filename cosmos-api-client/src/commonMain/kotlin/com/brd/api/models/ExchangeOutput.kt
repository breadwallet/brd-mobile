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
sealed class ExchangeOutput {

    abstract val media: ExchangeInput.Media
    abstract val amount: String
    abstract val actions: List<ExchangeOrder.Action>
    abstract val currency: ExchangeCurrency

    @Serializable
    @SerialName("crypto_transfer")
    data class CryptoTransfer(
        override val media: ExchangeInput.Media,
        override val amount: String,
        override val actions: List<ExchangeOrder.Action>,
        override val currency: ExchangeCurrency,
        @SerialName("expires_at")
        @Serializable(with = InstantSerializer::class)
        val expiresAt: Instant? = null,
        @SerialName("crypto_transfer_status")
        val status: Status,
        @SerialName("send_to_address")
        val sendToAddress: String,
        @SerialName("transaction_id")
        val transactionId: String,
    ) : ExchangeOutput() {

        @Serializable
        enum class Status {
            @SerialName("waiting_for_address")
            WAITING_FOR_ADDRESS,

            @SerialName("ready")
            READY,

            @SerialName("failed")
            FAILED,

            @SerialName("processing")
            PROCESSING,

            @SerialName("complete")
            COMPLETE,
        }
    }

    @SerialName("ach")
    @Serializable
    data class Ach(
        override val media: ExchangeInput.Media,
        override val amount: String,
        override val actions: List<ExchangeOrder.Action>,
        override val currency: ExchangeCurrency,
        @SerialName("expires_at")
        @Serializable(with = InstantSerializer::class)
        val expiresAt: Instant? = null,
        @SerialName("ach_transfer_status")
        val status: Status,
    ) : ExchangeOutput() {

        @Serializable
        enum class Status {
            @SerialName("waiting_for_media")
            WAITING_FOR_MEDIA,

            @SerialName("ready")
            READY,

            @SerialName("failed")
            FAILED,

            @SerialName("processing")
            PROCESSING,

            @SerialName("complete")
            COMPLETE,
        }
    }
}