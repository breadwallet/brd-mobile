/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/05/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import com.breadwallet.ui.settings.segwit.LegacyAddress.F
import com.breadwallet.ui.settings.segwit.LegacyAddress.M
import com.spotify.mobius.First
import com.spotify.mobius.Init

object LegacyAddressInit : Init<M, F> {
    override fun init(model: M): First<M, F> =
        First.first(model, setOf(F.LoadAddress))
}
