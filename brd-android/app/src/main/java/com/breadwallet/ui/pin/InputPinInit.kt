/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 9/23/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.pin

import com.breadwallet.ui.pin.InputPin.F
import com.breadwallet.ui.pin.InputPin.M
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

object InputPinInit : Init<M, F> {
    override fun init(model: M): First<M, F> {
        return first(
            model, effects<F, F>(
                F.CheckIfPinExists
            )
        )
    }
}
