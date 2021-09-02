/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/25/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.manager

import android.annotation.TargetApi
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@TargetApi(Build.VERSION_CODES.N)
class NetworkCallbacksConnectivityStateProvider(
    private val connectivityManager: ConnectivityManager
) : ConnectivityStateProvider, ConnectivityManager.NetworkCallback() {

    private val _state = MutableStateFlow(getConnectivityState())

    init {
        connectivityManager.registerDefaultNetworkCallback(this)
    }

    override fun state(): Flow<ConnectivityState> = _state

    override fun close() {
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
        _state.value = getConnectivityState(capabilities)
    }

    override fun onLost(network: Network) {
        _state.value = ConnectivityState.Disconnected
    }

    private fun getConnectivityState() =
        getConnectivityState(connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork))

    private fun getConnectivityState(capabilities: NetworkCapabilities?) = if (capabilities != null) {
        ConnectivityState.Connected
    } else {
        ConnectivityState.Disconnected
    }
}
