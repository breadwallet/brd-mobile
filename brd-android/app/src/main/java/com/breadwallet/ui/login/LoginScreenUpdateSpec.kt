/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.login

import com.spotify.mobius.Next

interface LoginScreenUpdateSpec {
    fun patch(model: LoginScreen.M, event: LoginScreen.E): Next<LoginScreen.M, LoginScreen.F> = when (event) {
        LoginScreen.E.OnFingerprintClicked -> onFingerprintClicked(model)
        LoginScreen.E.OnPinLocked -> onPinLocked(model)
        LoginScreen.E.OnUnlockAnimationEnd -> onUnlockAnimationEnd(model)
        LoginScreen.E.OnAuthenticationSuccess -> onAuthenticationSuccess(model)
        LoginScreen.E.OnAuthenticationFailed -> onAuthenticationFailed(model)
        is LoginScreen.E.OnFingerprintEnabled -> onFingerprintEnabled(model, event)
    }

    fun onFingerprintClicked(model: LoginScreen.M): Next<LoginScreen.M, LoginScreen.F>

    fun onPinLocked(model: LoginScreen.M): Next<LoginScreen.M, LoginScreen.F>

    fun onUnlockAnimationEnd(model: LoginScreen.M): Next<LoginScreen.M, LoginScreen.F>

    fun onAuthenticationSuccess(model: LoginScreen.M): Next<LoginScreen.M, LoginScreen.F>

    fun onAuthenticationFailed(model: LoginScreen.M): Next<LoginScreen.M, LoginScreen.F>

    fun onFingerprintEnabled(model: LoginScreen.M, event: LoginScreen.E.OnFingerprintEnabled): Next<LoginScreen.M, LoginScreen.F>
}
