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

/**
 * Represents a specific region within an [ExchangeCountry] to
 * provide more accurate [ExchangePair]s.
 */
@Serializable
data class ExchangeRegion(
    val code: String,
    val name: String,
)
