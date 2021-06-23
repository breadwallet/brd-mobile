/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

import kotlinx.serialization.Serializable

sealed class BrdCurrenciesResult {
    @Serializable
    data class Success(
        val currencies: List<BrdCurrency>
    ) : BrdCurrenciesResult()

    data class Error(
        override val status: Int,
        override val body: String
    ) : BrdCurrenciesResult(), ApiResponseError
}
