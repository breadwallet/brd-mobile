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

sealed class ExchangeOrderResult {

    @Serializable
    data class Success(val order: ExchangeOrder) : ExchangeOrderResult()

    data class Error(
        override val status: Int,
        override val body: String,
        val type: ErrorType?,
        val message: String?,
    ) : ExchangeOrderResult(), ApiResponseError

    @Serializable
    enum class ErrorType {
        @SerialName("order_create_expired")
        ORDER_CREATE_EXPIRED,
        @SerialName("provider_error")
        PROVIDER_ERROR,
        @SerialName("unknown_error")
        UNKNOWN_ERROR,
    }
}
