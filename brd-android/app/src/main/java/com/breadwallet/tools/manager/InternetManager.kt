/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/25/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
@file:Suppress("DEPRECATION")
package com.breadwallet.tools.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InternetManager(
    private val connectivityManager: ConnectivityManager,
    private val context: Context
) : BroadcastReceiver(), ConnectivityStateProvider {

    private val _state = MutableStateFlow(isConnected())

    init {
        context.registerReceiver(
            this@InternetManager,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun state(): Flow<ConnectivityState> = _state

    override fun close() {
        context.unregisterReceiver(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val networkInfo =
                intent.getParcelableExtra<NetworkInfo>(ConnectivityManager.EXTRA_NETWORK_INFO)
            if (networkInfo != null && networkInfo.detailedState == NetworkInfo.DetailedState.CONNECTED) {
                _state.value = ConnectivityState.Connected
            } else if (networkInfo != null && networkInfo.detailedState == NetworkInfo.DetailedState.DISCONNECTED) {
                _state.value = ConnectivityState.Disconnected
            }
        }
    }

    fun isConnected() = when (connectivityManager.activeNetworkInfo?.isConnectedOrConnecting) {
        true -> ConnectivityState.Connected
        else -> ConnectivityState.Disconnected
    }
}