/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.bakerapi.models

import kotlinx.serialization.Serializable

@Serializable
sealed class BakerResult {
    @Serializable
    data class Success(
        val baker: Baker
    ) : BakerResult()

    sealed class Error : BakerResult() {
        data class HttpError(
            val status: Int,
            val body: String
        ): Error()

        data class ResponseError(
            val message: String
        ) : Error()
    }
}