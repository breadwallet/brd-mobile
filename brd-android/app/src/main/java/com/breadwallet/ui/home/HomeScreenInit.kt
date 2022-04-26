/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 8/1/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.home

import com.breadwallet.ui.home.HomeScreen.F
import com.breadwallet.ui.home.HomeScreen.M
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

val HomeScreenInit = Init<M, F> { model ->
    first(
        model,
        effects(
            F.LoadEnabledWallets,
            F.LoadIsBuyBellNeeded,
            F.LoadIsBuyPromoDotNeeded,
            F.LoadIsTradePromoDotNeeded,
            F.LoadPrompt,
            F.CheckIfShowBuyAndSell,
            F.LoadConnectivityState
        )
    )
}
