/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.fingerprint

import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.F
import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.M
import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

object FingerprintSettingsInit : Init<M, F> {
    override fun init(model: M): First<M, F> =
        first(model, setOf(F.LoadCurrentSettings))
}
