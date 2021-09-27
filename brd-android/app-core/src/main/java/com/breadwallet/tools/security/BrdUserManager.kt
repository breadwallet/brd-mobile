/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.security

import com.blockset.walletkit.Account
import kotlinx.coroutines.flow.Flow

/** Manages creation, recovery, and access to an [Account]. */
@Suppress("TooManyFunctions")
interface BrdUserManager {
    suspend fun setupWithGeneratedPhrase(): SetupResult
    suspend fun setupWithPhrase(phrase: ByteArray): SetupResult
    suspend fun migrateKeystoreData(): Boolean

    suspend fun checkAccountInvalidated()

    fun isInitialized(): Boolean
    fun getState(): BrdUserState
    fun stateChanges(disabledUpdates: Boolean = false): Flow<BrdUserState>

    fun isMigrationRequired(): Boolean

    suspend fun getPhrase(): ByteArray?
    fun getAccount(): Account?
    fun updateAccount(accountBytes: ByteArray)
    fun getAuthKey(): ByteArray?

    suspend fun configurePinCode(pinCode: String)
    suspend fun clearPinCode(phrase: ByteArray)
    fun verifyPinCode(pinCode: String): Boolean
    fun hasPinCode(): Boolean
    fun pinCodeNeedsUpgrade(): Boolean

    fun lock()
    fun unlock()

    fun getToken(): String?
    fun putToken(token: String)
    fun removeToken()

    fun getBdbJwt(): String?
    fun putBdbJwt(jwt: String, exp: Long)

    fun onActivityResult(requestCode: Int, resultCode: Int)
}
