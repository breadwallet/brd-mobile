/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.nodeselector

import com.spotify.mobius.Next

interface NodeSelectorUpdateSpec {
    fun patch(model: NodeSelector.M, event: NodeSelector.E): Next<NodeSelector.M, NodeSelector.F> = when (event) {
        NodeSelector.E.OnSwitchButtonClicked -> onSwitchButtonClicked(model)
        is NodeSelector.E.OnConnectionStateUpdated -> onConnectionStateUpdated(model, event)
        is NodeSelector.E.OnConnectionInfoLoaded -> onConnectionInfoLoaded(model, event)
        is NodeSelector.E.SetCustomNode -> setCustomNode(model, event)
    }

    fun onSwitchButtonClicked(model: NodeSelector.M): Next<NodeSelector.M, NodeSelector.F>

    fun onConnectionStateUpdated(model: NodeSelector.M, event: NodeSelector.E.OnConnectionStateUpdated): Next<NodeSelector.M, NodeSelector.F>

    fun onConnectionInfoLoaded(model: NodeSelector.M, event: NodeSelector.E.OnConnectionInfoLoaded): Next<NodeSelector.M, NodeSelector.F>

    fun setCustomNode(model: NodeSelector.M, event: NodeSelector.E.SetCustomNode): Next<NodeSelector.M, NodeSelector.F>
}