/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 4/27/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.writedownkey

import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.writedownkey.WriteDownKey.F
import com.breadwallet.ui.writedownkey.WriteDownKey.M
import com.spotify.mobius.First
import com.spotify.mobius.Init

object WriteDownKeyInit : Init<M, F> {
    override fun init(model: M): First<M, F> =
        First.first(model, setOf(F.TrackEvent(EventUtils.EVENT_PAPER_KEY_INTRO_APPEARED)))
}
