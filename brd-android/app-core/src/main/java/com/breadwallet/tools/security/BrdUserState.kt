/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/11/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.security

sealed class BrdUserState {
    data class Disabled(
        val seconds: Int
    ) : BrdUserState()

    object Enabled : BrdUserState()
    object Locked : BrdUserState()
    object Uninitialized : BrdUserState()
    sealed class KeyStoreInvalid : BrdUserState() {
        object Wipe : KeyStoreInvalid()
        object Uninstall : KeyStoreInvalid()
        object Lock : KeyStoreInvalid()
    }
}