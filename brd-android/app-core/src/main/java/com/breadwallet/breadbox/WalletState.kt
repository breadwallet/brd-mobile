/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 04/28/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.breadbox

sealed class WalletState {
    object Loading : WalletState()
    object Initialized : WalletState()
    object WaitingOnAction : WalletState()
    object Error : WalletState()
}