/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.fingerprint

import com.spotify.mobius.Next

interface FingerprintSettingsUpdateSpec {
    fun patch(model: FingerprintSettings.M, event: FingerprintSettings.E): Next<FingerprintSettings.M, FingerprintSettings.F> = when (event) {
        FingerprintSettings.E.OnBackClicked -> onBackClicked(model)
        FingerprintSettings.E.OnFaqClicked -> onFaqClicked(model)
        is FingerprintSettings.E.OnAppUnlockChanged -> onAppUnlockChanged(model, event)
        is FingerprintSettings.E.OnSendMoneyChanged -> onSendMoneyChanged(model, event)
        is FingerprintSettings.E.OnSettingsLoaded -> onSettingsLoaded(model, event)
    }

    fun onBackClicked(model: FingerprintSettings.M): Next<FingerprintSettings.M, FingerprintSettings.F>

    fun onFaqClicked(model: FingerprintSettings.M): Next<FingerprintSettings.M, FingerprintSettings.F>

    fun onAppUnlockChanged(model: FingerprintSettings.M, event: FingerprintSettings.E.OnAppUnlockChanged): Next<FingerprintSettings.M, FingerprintSettings.F>

    fun onSendMoneyChanged(model: FingerprintSettings.M, event: FingerprintSettings.E.OnSendMoneyChanged): Next<FingerprintSettings.M, FingerprintSettings.F>

    fun onSettingsLoaded(model: FingerprintSettings.M, event: FingerprintSettings.E.OnSettingsLoaded): Next<FingerprintSettings.M, FingerprintSettings.F>
}