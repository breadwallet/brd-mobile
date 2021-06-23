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
sealed class CurrencyMethod {
    abstract val status: Status

    /** Pay in method description. */
    abstract val description: String

    /** Verbose message to display to the user */
    abstract val message: String


    @Serializable
    @SerialName("sepa")
    data class Sepa(
        override val status: Status,
        override val message: String,
        override val description: String,
    ) : CurrencyMethod()

    @Serializable
    @SerialName("card")
    data class Card(
        override val status: Status,
        override val message: String,
        override val description: String,
    ) : CurrencyMethod()

    @Serializable
    @SerialName("ach")
    data class Ach(
        override val status: Status,
        override val message: String,
        override val description: String,
    ) : CurrencyMethod()

    @Serializable
    @SerialName("crypto")
    data class Crypto(
        override val status: Status,
        override val message: String,
        override val description: String,
        val address: String? = null,
        @SerialName("transaction_id")
        val transactionId: String? = null,
        @SerialName("refund_address")
        val refundAddress: String? = null,
    ) : CurrencyMethod()

    @Serializable
    enum class Status {
        @SerialName("pending")
        PENDING,

        @SerialName("ready")
        READY,
    }
}
