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
data class ExchangeOrder(
    val url: String,
    @SerialName("order_id")
    val orderId: String,
    val inputs: List<ExchangeInput>,
    val outputs: List<ExchangeOutput>,
    val status: Status,
    @SerialName("country_code")
    val countryCode: String,
    @SerialName("region_code")
    val regionCode: String?,
    val provider: ExchangeOffer.Provider,
    val test: Boolean = false,
    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @SerialName("expires_at")
    @Serializable(with = InstantSerializer::class)
    val expiresAt: Instant? = null,
) {
    @Serializable
    enum class Status {
        @SerialName("initializing")
        INITIALIZING,

        @SerialName("initialized")
        INITIALIZED,

        @SerialName("finalized")
        FINALIZED,
    }

    @Serializable
    sealed class Action {
        abstract val url: String
        abstract val title: String
        abstract val message: String

        @Serializable
        @SerialName("crypto_receive_address")
        data class CryptoReceiveAddress(
            override val url: String,
            override val title: String,
            override val message: String,
        ) : Action()

        @Serializable
        @SerialName("crypto_refund_address")
        data class CryptoRefundAddress(
            override val url: String,
            override val title: String = "",
            override val message: String = "",
        ) : Action()

        @Serializable
        @SerialName("crypto_send")
        data class CryptoSend(
            override val url: String,
            override val title: String,
            override val message: String,
        ) : Action()

        @Serializable
        @SerialName("browser")
        data class Browser(
            override val url: String,
            override val title: String,
            override val message: String,
        ) : Action()
    }
}
