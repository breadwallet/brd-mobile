/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

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
    val createdAt: Instant,
    @SerialName("expires_at")
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
    data class Action(
        val url: String,
        val title: String,
        val message: String,
        val type: Type,
    ) {

        @Serializable
        enum class Type {
            @SerialName("crypto_receive_address")
            CRYPTO_RECEIVE_ADDRESS,

            @SerialName("crypto_refund_address")
            CRYPTO_REFUND_ADDRESS,

            @SerialName("crypto_send")
            CRYPTO_SEND,

            @SerialName("browser")
            BROWSER,
        }
    }
}
