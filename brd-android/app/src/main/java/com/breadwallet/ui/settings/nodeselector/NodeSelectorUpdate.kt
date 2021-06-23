/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/14/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.nodeselector

import com.breadwallet.crypto.WalletManagerState
import com.breadwallet.ui.settings.nodeselector.NodeSelector.E
import com.breadwallet.ui.settings.nodeselector.NodeSelector.F
import com.breadwallet.ui.settings.nodeselector.NodeSelector.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

object NodeSelectorUpdate : Update<M, E, F>, NodeSelectorUpdateSpec {

    override fun update(
        model: M,
        event: E
    ): Next<M, F> = patch(model, event)

    override fun onSwitchButtonClicked(model: M): Next<M, F> {
        return dispatch(
            if (model.mode == NodeSelector.Mode.AUTOMATIC) {
                setOf(F.ShowNodeDialog)
            } else {
                setOf(F.SetToAutomatic)
            }
        )
    }

    override fun setCustomNode(
        model: M,
        event: E.SetCustomNode
    ): Next<M, F> =
        next(
            model.copy(
                mode = NodeSelector.Mode.AUTOMATIC,
                currentNode = event.node
            ),
            setOf(F.SetCustomNode(event.node))
        )

    override fun onConnectionStateUpdated(
        model: M,
        event: E.OnConnectionStateUpdated
    ): Next<M, F> =
        next(
            model.copy(
                connected = when (event.state) {
                    WalletManagerState.SYNCING(), WalletManagerState.CONNECTED() -> true
                    else -> false
                }
            )
        )

    override fun onConnectionInfoLoaded(
        model: M,
        event: E.OnConnectionInfoLoaded
    ): Next<M, F> =
        next(model.copy(mode = event.mode, currentNode = event.node))
}
