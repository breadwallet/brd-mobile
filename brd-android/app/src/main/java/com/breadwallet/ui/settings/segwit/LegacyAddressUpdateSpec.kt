/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import com.spotify.mobius.Next

interface LegacyAddressUpdateSpec {
    fun patch(model: LegacyAddress.M, event: LegacyAddress.E): Next<LegacyAddress.M, LegacyAddress.F> = when (event) {
        LegacyAddress.E.OnShareClicked -> onShareClicked(model)
        LegacyAddress.E.OnAddressClicked -> onAddressClicked(model)
        LegacyAddress.E.OnCloseClicked -> onCloseClicked(model)
        is LegacyAddress.E.OnAddressUpdated -> onAddressUpdated(model, event)
        is LegacyAddress.E.OnWalletNameUpdated -> onWalletNameUpdated(model, event)
    }

    fun onShareClicked(model: LegacyAddress.M): Next<LegacyAddress.M, LegacyAddress.F>

    fun onAddressClicked(model: LegacyAddress.M): Next<LegacyAddress.M, LegacyAddress.F>

    fun onCloseClicked(model: LegacyAddress.M): Next<LegacyAddress.M, LegacyAddress.F>

    fun onAddressUpdated(model: LegacyAddress.M, event: LegacyAddress.E.OnAddressUpdated): Next<LegacyAddress.M, LegacyAddress.F>

    fun onWalletNameUpdated(model: LegacyAddress.M, event: LegacyAddress.E.OnWalletNameUpdated): Next<LegacyAddress.M, LegacyAddress.F>
}
