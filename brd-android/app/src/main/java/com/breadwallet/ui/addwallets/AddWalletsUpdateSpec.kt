/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.addwallets

import com.spotify.mobius.Next

interface AddWalletsUpdateSpec {
    fun patch(model: AddWallets.M, event: AddWallets.E): Next<AddWallets.M, AddWallets.F> = when (event) {
        AddWallets.E.OnBackClicked -> onBackClicked(model)
        is AddWallets.E.OnSearchQueryChanged -> onSearchQueryChanged(model, event)
        is AddWallets.E.OnTokensChanged -> onTokensChanged(model, event)
        is AddWallets.E.OnAddWalletClicked -> onAddWalletClicked(model, event)
        is AddWallets.E.OnRemoveWalletClicked -> onRemoveWalletClicked(model, event)
    }

    fun onBackClicked(model: AddWallets.M): Next<AddWallets.M, AddWallets.F>

    fun onSearchQueryChanged(model: AddWallets.M, event: AddWallets.E.OnSearchQueryChanged): Next<AddWallets.M, AddWallets.F>

    fun onTokensChanged(model: AddWallets.M, event: AddWallets.E.OnTokensChanged): Next<AddWallets.M, AddWallets.F>

    fun onAddWalletClicked(model: AddWallets.M, event: AddWallets.E.OnAddWalletClicked): Next<AddWallets.M, AddWallets.F>

    fun onRemoveWalletClicked(model: AddWallets.M, event: AddWallets.E.OnRemoveWalletClicked): Next<AddWallets.M, AddWallets.F>
}
