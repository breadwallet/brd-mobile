/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 12/09/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.uigift

import com.breadwallet.ui.uigift.CreateGift.F
import com.breadwallet.ui.uigift.CreateGift.M
import com.spotify.mobius.First
import com.spotify.mobius.Init

object CreateGiftInit : Init<M, F> {
    override fun init(model: M): First<M, F> =
        First.first(model, setOf(F.CreatePaperWallet))
}
