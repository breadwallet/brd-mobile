/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/14/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings

import com.breadwallet.ui.settings.SettingsScreen.F
import com.breadwallet.ui.settings.SettingsScreen.M
import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

object SettingsInit : Init<M, F> {
    override fun init(model: M): First<M, F> =
        first(model, setOf(F.LoadOptions(model.section)))
}
