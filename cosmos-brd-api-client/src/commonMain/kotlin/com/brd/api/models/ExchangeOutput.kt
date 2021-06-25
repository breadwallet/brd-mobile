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