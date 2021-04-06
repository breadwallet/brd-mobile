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
sealed class ExchangeInput {
    abstract val media: Media
    abstract val amount: String
    abstract val currency: ExchangeCurrency
    @SerialName("expires_at")
    @Serializable(with = InstantSerializer::class)
    abstract val expiresAt: Instant?

    @Serializable
    @SerialName("crypto_transfer")
    data class CryptoTransfer(
        override val media: Media,
        override val amount: String,
        override val currency: ExchangeCurrency,
        @SerialName("expires_at")
        @Serializable(with = InstantSerializer::class)
        override val expiresAt: Instant? = null,
        @SerialName("crypto_transfer_status")
        val status: CryptoStatus,
        @SerialName("send_to_address")
        val sendToAddress: String,
        @SerialName("refund_address")
        val refundAddress: String,
        @SerialName("payment_detected_time")
        @Serializable(with = InstantSerializer::class)
        val paymentDetectedTime: Instant? = null,
        @SerialName("transaction_id")
        val transactionId: String,
        @SerialName("confirmation_detected_time")
        @Serializable(with = InstantSerializer::class)
        val confirmationDetectedTime: Instant? = null,
        @SerialName("refunded_transaction_id")
        val refundedTransactionId: String,
    ) : ExchangeInput()

    @Serializable
    @SerialName("card_payment")
    data class CardPayment(
        override val media: Media,
        override val amount: String,
        override val currency: ExchangeCurrency,
        @SerialName("expires_at")
        @Serializable(with = InstantSerializer::class)
        override val expiresAt: Instant?,
    ) : ExchangeInput()

    @Serializable
    @SerialName("ach")
    data class Ach(
        override val media: Media,
        override val amount: String,
        override val currency: ExchangeCurrency,
        @SerialName("expires_at")
        @Serializable(with = InstantSerializer::class)
        override val expiresAt: Instant?,
        @SerialName("ach_transfer_status")
        val status: FiatStatus,
    ) : ExchangeInput()

    @Serializable
    @SerialName("sepa")
    data class Sepa(
        override val media: Media,
        override val amount: String,
        override val currency: ExchangeCurrency,
        @SerialName("expires_at")
        @Serializable(with = InstantSerializer::class)
        override val expiresAt: Instant?,
    ) : ExchangeInput()

    @Serializable
    enum class Media {
        @SerialName("crypto")
        CRYPTO,
        @SerialName("ach")
        ACH,
        @SerialName("card")
        CARD,
        @SerialName("sepa")
        SEPA,
    }

    @Serializable
    enum class CryptoStatus {
        @SerialName("waiting_for_payment")
        WAITING_FOR_PAYMENT,

        @SerialName("waiting_for_confirmation")
        WAITING_FOR_CONFIRMATION,

        @SerialName("waiting_for_address")
        WAITING_FOR_ADDRESS,

        @SerialName("refunded")
        REFUNDED,

        @SerialName("complete")
        COMPLETE,
    }

    @Serializable
    enum class FiatStatus {
        @SerialName("waiting_for_payment")
        WAITING_FOR_PAYMENT,

        @SerialName("waiting_for_authorization")
        WAITING_FOR_AUTHORIZATION,

        @SerialName("failed")
        FAILED,

        @SerialName("pending")
        PENDING,

        @SerialName("complete")
        COMPLETE,
    }
}