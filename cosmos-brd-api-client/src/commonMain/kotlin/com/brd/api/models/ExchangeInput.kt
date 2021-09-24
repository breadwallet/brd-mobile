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
sealed class ExchangeInput {
    abstract val media: Media
    abstract val amount: String
    abstract val currency: ExchangeCurrency
    abstract val actions: List<ExchangeOrder.Action>
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
        override val actions: List<ExchangeOrder.Action>,
        @SerialName("crypto_transfer_status")
        val status: CryptoStatus,
        @SerialName("send_to_address")
        val sendToAddress: String,
        @SerialName("send_to_destination_tag")
        val sendToDestinationTag: String?,
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
        override val actions: List<ExchangeOrder.Action>,
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
        override val actions: List<ExchangeOrder.Action>,
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
        override val actions: List<ExchangeOrder.Action>,
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

        @SerialName("failed")
        FAILED,

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
