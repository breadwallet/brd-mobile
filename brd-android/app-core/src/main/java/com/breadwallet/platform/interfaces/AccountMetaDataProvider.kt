/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 9/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.platform.interfaces

import com.breadwallet.protocols.messageexchange.entities.PairingMetaData
import kotlinx.coroutines.flow.Flow
import com.breadwallet.platform.entities.TxMetaData
import com.breadwallet.platform.entities.WalletInfoData
import com.blockset.walletkit.Account
import com.blockset.walletkit.Transfer
import com.blockset.walletkit.WalletManagerMode
import com.breadwallet.platform.entities.TxMetaDataValue
import java.util.Date

@Suppress("TooManyFunctions")
/** Manages access to metadata for an [Account]. */
interface AccountMetaDataProvider {
    // TODO: Refine/refactor this API. For now it includes all leftover KVStoreManager functions

    /** Initializes metadata for a newly created [Account]. */
    fun create(accountCreationDate: Date)

    /**
     * Recovers all metadata for a recovered [Account] and returns true if successful.
     * If [migrate] is true, any metadata migrations to the latest schema will be performed.
     */
    suspend fun recoverAll(migrate: Boolean = false): Boolean

    /** Returns a [Flow] of enabled wallet currency ids (addresses), will recover them if necessary. */
    fun enabledWallets(): Flow<List<String>>

    /** Returns a list of enabled wallet currency ids (addresses) if recovered, or null when not. */
    fun getEnabledWalletsUnsafe(): List<String>?

    /** Returns a [Flow] of [WalletInfoData], will recover it if necessary. */
    fun walletInfo(): Flow<WalletInfoData>

    /** Returns [WalletInfoData] if recovered, or null when it is not. */
    fun getWalletInfoUnsafe(): WalletInfoData?

    /** Enables the wallet for this Account. */
    fun enableWallet(currencyId: String)

    fun enableWallets(currencyIds: List<String>)

    /** Disables the wallet for this Account. */
    fun disableWallet(currencyId: String)

    /** Reorders the wallet display order */
    fun reorderWallets(currencyIds: List<String>)

    /** Persists [mode] for the wallet. */
    suspend fun putWalletMode(currencyId: String, mode: WalletManagerMode)

    /** Returns a map of currencyId to [WalletManagerMode]. */
    fun walletModes(): Flow<Map<String, WalletManagerMode>>

    /** Clean up. */
    //fun close()

    fun txMetaData(onlyGifts: Boolean = false): Map<String, TxMetaData>

    /** Returns a [Flow] of [TxMetaData] associated with [transaction], will recover it if necessary. */
    fun txMetaData(transaction: Transfer): Flow<TxMetaData>

    fun txMetaData(key: String, isErc20: Boolean): Flow<TxMetaData>

    /** Persist given [TxMetaData] for [transaction], but ONLY if the comment or exchange rate has changed. */
    suspend fun putTxMetaData(transaction: Transfer, newTxMetaData: TxMetaDataValue)

    suspend fun putTxMetaData(key: String, isErc20: Boolean, newTxMetaData: TxMetaDataValue)

    fun getPairingMetadata(pubKey: ByteArray): PairingMetaData?

    fun putPairingMetadata(pairingData: PairingMetaData): Boolean

    /** Returns the last cursor (used in retrieving inbox messages). */
    fun getLastCursor(): String?

    /** Persists the last cursor (used in retrieving inbox messages). */
    fun putLastCursor(lastCursor: String): Boolean

    /** Restore the list of enabled wallets to the defaults. */
    fun resetDefaultWallets()
}
