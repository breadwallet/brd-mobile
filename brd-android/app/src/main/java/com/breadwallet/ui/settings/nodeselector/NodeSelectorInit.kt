/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/14/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.nodeselector

import com.breadwallet.ui.settings.nodeselector.NodeSelector.F
import com.breadwallet.ui.settings.nodeselector.NodeSelector.M
import com.spotify.mobius.First
import com.spotify.mobius.Init

object NodeSelectorInit : Init<M, F> {
    override fun init(model: M): First<M, F> =
        First.first(model, setOf(F.LoadConnectionInfo))
}
