/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 04/28/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.breadbox

import com.breadwallet.crypto.Network
import com.breadwallet.crypto.System
import com.breadwallet.crypto.errors.AccountInitializationError

interface NetworkInitializer {
    suspend fun initialize(system: System, network: Network, createIfNeeded: Boolean): NetworkState
    fun isSupported(currencyId: String): Boolean
}

sealed class NetworkState {
    object Initialized : NetworkState()
    object Loading : NetworkState()
    object ActionNeeded : NetworkState()

    data class Error(
        val error: AccountInitializationError? = null,
        val message: String? = null
    ) : NetworkState()
}

