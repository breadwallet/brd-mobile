/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/05/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import com.breadwallet.ui.settings.segwit.LegacyAddress.E
import com.breadwallet.ui.settings.segwit.LegacyAddress.F
import com.breadwallet.ui.settings.segwit.LegacyAddress.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

object LegacyAddressUpdate :
    Update<M, E, F>,
    LegacyAddressUpdateSpec {

    override fun update(
        model: M,
        event: E
    ): Next<M, F> = patch(model, event)

    override fun onShareClicked(model: M): Next<M, F> =
        dispatch(setOf(F.ShareAddress(model.receiveAddress, model.walletName)))

    override fun onAddressClicked(model: M): Next<M, F> =
        dispatch(setOf(F.CopyAddressToClipboard(model.sanitizedAddress)))

    override fun onCloseClicked(model: M): Next<M, F> =
        dispatch(setOf(F.GoBack))

    override fun onAddressUpdated(
        model: M,
        event: E.OnAddressUpdated
    ): Next<M, F> =
        next(
            model.copy(
                receiveAddress = event.receiveAddress,
                sanitizedAddress = event.sanitizedAddress
            )
        )

    override fun onWalletNameUpdated(
        model: M,
        event: E.OnWalletNameUpdated
    ): Next<M, F> =
        next(model.copy(walletName = event.name))
}
