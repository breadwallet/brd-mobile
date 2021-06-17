/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.login

import com.breadwallet.ui.login.LoginScreen.F
import com.breadwallet.ui.login.LoginScreen.M
import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

object LoginInit : Init<M, F> {
    override fun init(model: M): First<M, F> {
        return first(model, setOf(F.CheckFingerprintEnable))
    }
}
