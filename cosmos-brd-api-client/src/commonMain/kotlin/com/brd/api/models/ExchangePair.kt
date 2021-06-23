/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.api.models

import com.brd.api.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ExchangePair(
    val fromCode: String,
    val toCode: String,
    val rate: Double,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
)
