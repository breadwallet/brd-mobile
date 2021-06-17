/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 12/6/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.fastsync

import com.breadwallet.ui.settings.fastsync.FastSync.F
import com.breadwallet.ui.settings.fastsync.FastSync.M
import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

object FastSyncInit : Init<M, F> {
    override fun init(model: M): First<M, F> =
        first(
            model,
            setOf(F.LoadCurrencyIds)
        )
}
