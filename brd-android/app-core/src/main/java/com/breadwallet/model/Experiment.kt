/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 8/14/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.model

/**
 * Response from me/experiments endpoint.
 */
data class Experiment(
        val id: Int,
        val name: String,
        val active: Boolean,
        val meta: String
)
