/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeCurrency(
    @SerialName("currency_id")
    val currencyId: String,
    val code: String,
    val name: String,
    val decimals: Int,
    val type: Type
) {
    fun isCrypto(): Boolean = type == Type.CRYPTO
    fun isFiat(): Boolean = type == Type.FIAT

    @Serializable
    enum class Type {
        @SerialName("crypto")
        CRYPTO,
        @SerialName("fiat")
        FIAT,
    }
}
