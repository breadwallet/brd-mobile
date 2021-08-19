/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.onboarding

import com.spotify.mobius.Next

interface OnBoardingUpdateSpec {
    fun patch(model: OnBoarding.M, event: OnBoarding.E): Next<OnBoarding.M, OnBoarding.F> = when (event) {
        OnBoarding.E.OnSkipClicked -> onSkipClicked(model)
        OnBoarding.E.OnBackClicked -> onBackClicked(model)
        OnBoarding.E.OnBuyClicked -> onBuyClicked(model)
        OnBoarding.E.OnBrowseClicked -> onBrowseClicked(model)
        OnBoarding.E.OnWalletCreated -> onWalletCreated(model)
        is OnBoarding.E.OnPageChanged -> onPageChanged(model, event)
        is OnBoarding.E.SetupError -> setupError(model, event)
    }

    fun onSkipClicked(model: OnBoarding.M): Next<OnBoarding.M, OnBoarding.F>

    fun onBackClicked(model: OnBoarding.M): Next<OnBoarding.M, OnBoarding.F>

    fun onBuyClicked(model: OnBoarding.M): Next<OnBoarding.M, OnBoarding.F>

    fun onBrowseClicked(model: OnBoarding.M): Next<OnBoarding.M, OnBoarding.F>

    fun onWalletCreated(model: OnBoarding.M): Next<OnBoarding.M, OnBoarding.F>

    fun onPageChanged(model: OnBoarding.M, event: OnBoarding.E.OnPageChanged): Next<OnBoarding.M, OnBoarding.F>

    fun setupError(model: OnBoarding.M, event: OnBoarding.E.SetupError): Next<OnBoarding.M, OnBoarding.F>
}
