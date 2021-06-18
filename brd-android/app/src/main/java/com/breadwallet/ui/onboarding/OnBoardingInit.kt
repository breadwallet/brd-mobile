/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.onboarding

import com.breadwallet.ui.onboarding.OnBoarding.F
import com.breadwallet.ui.onboarding.OnBoarding.M
import com.spotify.mobius.First
import com.spotify.mobius.Init

object OnBoardingInit : Init<M, F> {
    override fun init(model: M): First<M, F> {
        return First.first(model)
    }
}
