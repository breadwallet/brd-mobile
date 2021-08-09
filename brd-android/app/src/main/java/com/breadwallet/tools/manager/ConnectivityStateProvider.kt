/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/25/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.manager

import kotlinx.coroutines.flow.Flow

sealed class ConnectivityState {
    object Connected : ConnectivityState()
    object Disconnected : ConnectivityState()
}

interface ConnectivityStateProvider {
    fun state(): Flow<ConnectivityState>
    fun close()
}
