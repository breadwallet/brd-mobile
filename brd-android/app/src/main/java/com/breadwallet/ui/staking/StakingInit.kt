/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/30/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.staking

import com.breadwallet.ui.staking.Staking.F
import com.breadwallet.ui.staking.Staking.M
import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

object StakingInit : Init<M, F> {
    override fun init(model: M): First<M, F> =
        first(
            model,
            setOf(
                F.LoadAccount,
                F.LoadAuthenticationSettings
            )
        )
}
