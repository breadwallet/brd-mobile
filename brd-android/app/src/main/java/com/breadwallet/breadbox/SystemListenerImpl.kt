/**
 * SystemListenerImpl
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 9/8/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.breadbox

import com.blockset.walletkit.Account
import com.blockset.walletkit.Network
import com.blockset.walletkit.Transfer
import com.blockset.walletkit.System
import com.blockset.walletkit.Wallet
import com.blockset.walletkit.WalletManager
import com.blockset.walletkit.WalletManagerState
import com.blockset.walletkit.events.network.NetworkEvent
import com.blockset.walletkit.events.system.SystemDiscoveredNetworksEvent
import com.blockset.walletkit.events.system.SystemEvent
import com.blockset.walletkit.events.system.SystemListener
import com.blockset.walletkit.events.system.SystemNetworkAddedEvent
import com.blockset.walletkit.events.transfer.TransferEvent
import com.blockset.walletkit.events.wallet.WalletEvent
import com.blockset.walletkit.events.wallet.WalletTransferAddedEvent
import com.blockset.walletkit.events.wallet.WalletTransferChangedEvent
import com.blockset.walletkit.events.wallet.WalletTransferDeletedEvent
import com.blockset.walletkit.events.wallet.WalletTransferSubmittedEvent
import com.blockset.walletkit.events.walletmanager.WalletManagerChangedEvent
import com.blockset.walletkit.events.walletmanager.WalletManagerCreatedEvent
import com.blockset.walletkit.events.walletmanager.WalletManagerEvent
import com.blockset.walletkit.events.walletmanager.WalletManagerSyncRecommendedEvent
import com.blockset.walletkit.events.walletmanager.WalletManagerWalletAddedEvent
import com.blockset.walletkit.events.walletmanager.WalletManagerWalletDeletedEvent
import com.breadwallet.logger.logDebug
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SystemListenerImpl : SystemListener {

    private var networkManager: NetworkManager? = null

    /** SharedFlow for All Event Handling for [SystemListener] */
    private val networkEvents = MutableSharedFlow<NetworkEvent>(replay = 1)

    private val _systemEvents = MutableSharedFlow<Unit>(replay = 1)
    val systemEvents: SharedFlow<Unit> = _systemEvents

    private val _transferEvents = MutableSharedFlow<TransferEvent>(replay = 1)
    val transferEvents: SharedFlow<TransferEvent> = _transferEvents

    private val _walletEvents = MutableSharedFlow<WalletEvent>(replay = 1)
    val walletEvents: SharedFlow<WalletEvent> = _walletEvents

    private val _walletManagerEvents = MutableSharedFlow<WalletManagerEvent>(replay = 1)
    val walletManagerEvents: SharedFlow<WalletManagerEvent> = _walletManagerEvents

    /** StateFlow for tracking values [SystemListener] */
    private val _discovery = MutableStateFlow(false)
    val discovery: StateFlow<Boolean> = _discovery

    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account

    private val _transfer = MutableSharedFlow<Transfer>(replay = 1)
    val transfer: SharedFlow<Transfer> = _transfer

    private val _transfers = MutableStateFlow<Map<Wallet, List<Transfer>>>(emptyMap())
    val transfers: StateFlow<Map<Wallet, List<Transfer>>> = _transfers

    fun init(networkManager: NetworkManager, system: System) {
        this.networkManager = networkManager
        _systemEvents.tryEmit(Unit)
        _account.value = system.account
    }

    override fun handleManagerEvent(
        system: System,
        manager: WalletManager,
        event: WalletManagerEvent
    ) {
        _walletManagerEvents.tryEmit(event)

        when (event) {
            is WalletManagerCreatedEvent -> {
                logDebug("Wallet Manager Created: '${manager.name}' mode ${manager.mode}")
                networkManager?.connectManager(manager)
            }
            is WalletManagerChangedEvent -> {
                if (event.isConnected(manager.currency.code)) {
                    logDebug("Wallet Manager Connected: '${manager.name}'")
                    networkManager?.registerCurrencies(manager)
                }
            }
            is WalletManagerSyncRecommendedEvent -> {
                logDebug("Syncing '${manager.currency.code}' to ${event.depth}")
                manager.syncToDepth(event.depth)
            }
            is WalletManagerWalletAddedEvent -> {
                _wallets.update { it + event.wallet }
            }
            is WalletManagerWalletDeletedEvent -> {
                _wallets.update { it - event.wallet }
            }
        }
    }

    override fun handleWalletEvent(
        system: System,
        manager: WalletManager,
        wallet: Wallet,
        event: WalletEvent
    ) {
        _walletEvents.tryEmit(event)

        when (event) {
            is WalletTransferChangedEvent -> _transfer.tryEmit(event.transfer)
            is WalletTransferSubmittedEvent -> _transfer.tryEmit(event.transfer)
            is WalletTransferDeletedEvent -> {
                _transfer.tryEmit(event.transfer)

                _transfers.update { walletMap ->
                    val newList = walletMap[wallet].orEmpty() - event.transfer
                    walletMap.toMutableMap()
                        .apply { set(wallet, newList) }
                }
            }
            is WalletTransferAddedEvent -> {
                _transfer.tryEmit(event.transfer)

                _transfers.update { walletMap ->
                    val newList = walletMap[wallet].orEmpty() + event.transfer
                    walletMap.toMutableMap()
                        .apply { set(wallet, newList) }
                        .toMap()
                }
            }
        }
    }

    override fun handleTransferEvent(
        system: System,
        manager: WalletManager,
        wallet: Wallet,
        transfer: Transfer,
        event: TransferEvent
    ) {
        _transferEvents.tryEmit(event)
        _transfer.tryEmit(transfer)
    }

    override fun handleNetworkEvent(system: System, network: Network, event: NetworkEvent) {
        networkEvents.tryEmit(event)
    }

    override fun handleSystemEvent(system: System, event: SystemEvent) {
        when (event) {
            is SystemNetworkAddedEvent -> {
                logDebug("Network '${event.network.name}' added.")
                networkManager?.initializeNetwork(event.network)
            }
            is SystemDiscoveredNetworksEvent -> {
                _discovery.value = true
            }
        }
        _systemEvents.tryEmit(Unit)
    }

    fun updateWallets(wallets: List<Wallet>?) {
        _wallets.update { (it + wallets.orEmpty()).distinct() }
    }

    private fun WalletManagerChangedEvent.isConnected(currencyCode: String): Boolean {
        val fromStateType = oldState.type
        val toStateType = newState.type
        logDebug("($currencyCode) State Changed from='$fromStateType' to='$toStateType'")

        return fromStateType != WalletManagerState.Type.CONNECTED &&
            toStateType == WalletManagerState.Type.CONNECTED
    }
}
