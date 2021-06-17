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
import com.breadwallet.crypto.errors.AccountInitializationCantCreateError
import com.breadwallet.crypto.errors.AccountInitializationError
import com.breadwallet.crypto.errors.AccountInitializationMultipleHederaAccountsError
import com.breadwallet.tools.security.BrdUserManager

class DefaultNetworkInitializer(private val userManager: BrdUserManager) : NetworkInitializer {
    override fun isSupported(currencyId: String) = true

    override suspend fun initialize(
        system: System,
        network: Network,
        createIfNeeded: Boolean
    ): NetworkState {
        if (system.accountIsInitialized(system.account, network)) {
            return NetworkState.Initialized
        }

        return try {
            val data = system.accountInitialize(system.account, network, createIfNeeded)
            userManager.updateAccount(data)
            NetworkState.Initialized
        } catch (e: AccountInitializationCantCreateError) {
            NetworkState.ActionNeeded
        } catch (e: AccountInitializationMultipleHederaAccountsError) {
            val account = e.accounts.sortedBy { it.balance.or(0) }[0]
            val data =
                system.accountInitializeUsingHedera(system.account, network, account).orNull()
            if (data == null) {
                NetworkState.Error(message = "Initialization failed using one of multiple Hedera accounts.")
            } else {
                userManager.updateAccount(data)
                NetworkState.Initialized
            }
        } catch (e: AccountInitializationError) {
            NetworkState.Error(e)
        }
    }
}
