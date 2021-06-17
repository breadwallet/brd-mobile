/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

import com.brd.api.internal.ArrayPackedObjectTransformer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class ExchangePairsResult {
    @Serializable
    data class Success(
        @SerialName("pair_count")
        val pairCount: Int,
        @SerialName("supported_pairs")
        @Serializable(with = SupportedPairsTransformer::class)
        val supportedPairs: List<ExchangePair>,
        val currencies: Map<String, ExchangeCurrency>,
    ) : ExchangePairsResult()

    data class Error(
        override val status: Int,
        override val body: String
    ) : ExchangePairsResult(), ApiResponseError
}

private object SupportedPairsTransformer : ArrayPackedObjectTransformer<ExchangePair>(ExchangePair.serializer())
