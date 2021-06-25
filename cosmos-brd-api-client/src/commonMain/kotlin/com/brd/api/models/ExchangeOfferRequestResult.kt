/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

sealed class ExchangeOfferRequestResult {

    data class Success(
        val offerRequest: ExchangeOfferRequest
    ) : ExchangeOfferRequestResult()

    data class Error(
        override val status: Int,
        override val body: String,
    ) : ExchangeOfferRequestResult(), ApiResponseError
}
