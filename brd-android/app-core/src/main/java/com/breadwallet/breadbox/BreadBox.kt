/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.breadbox

import com.blockset.walletkit.Account
import com.blockset.walletkit.Network

import com.blockset.walletkit.Wallet
import com.blockset.walletkit.System
import com.blockset.walletkit.Transfer
import kotlinx.coroutines.flow.Flow

/** Provides access to data from a lazily created [System] using [Flow]s. */
@Suppress("TooManyFunctions")
interface BreadBox {

    /** True when all systems are expected to be running and a [System] is available. */
    val isOpen: Boolean

    val isMainnet: Boolean

    /** Create and configure [System] and start receiving events. */
    fun open(account: Account)

    /** Cleanup [System] and stop emitting events. */
    fun close(wipe: Boolean = false)

    /** Emits the [System] objects produced when calling [open]. */
    fun system(): Flow<System>

    /** Emits the [Account] provided to [open]. */
    fun account(): Flow<Account>

    /** Emits the [Wallet]s created by the [System], defaults to only those tracked. */
    fun wallets(filterByTracked: Boolean = true): Flow<List<Wallet>>

    /** Emits the list of tracked currency codes. */
    fun currencyCodes(): Flow<List<String>>

    /** Emits the [Wallet] for [currencyCode]. */
    fun wallet(currencyCode: String): Flow<Wallet>

    /** Emits the [List] of [Transfer]s for the [Wallet] of [currencyCode]. */
    fun walletTransfers(currencyCode: String): Flow<List<Transfer>>

    /** Emits the [Transfer] for the [transferHash] of [currencyCode]. */
    fun walletTransfer(currencyCode: String, transferHash: String): Flow<Transfer>

    fun walletTransfer(currencyCode: String, transfer: Transfer): Flow<Transfer>

    /** Initializes the [Wallet] of [currencyCode], required for certain [Wallet]s before use. */
    fun initializeWallet(currencyCode: String)

    /** Emits the [WalletState] for the [Wallet] of [currencyCode]. */
    fun walletState(currencyCode: String): Flow<WalletState>

    /**
     * Emits the [Network]s discovered by [System].
     *
     * Setting [whenDiscoveryComplete] to true delays the first
     * emission until network discovery is complete.
     */
    fun networks(whenDiscoveryComplete: Boolean = false): Flow<List<Network>>

    /** Returns [System] when [isOpen] or null when it is not. */
    fun getSystemUnsafe(): System?
}
