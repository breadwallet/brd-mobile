/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.onboarding

import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.onboarding.OnBoarding.E
import com.breadwallet.ui.onboarding.OnBoarding.E.SetupError
import com.breadwallet.ui.onboarding.OnBoarding.F
import com.breadwallet.ui.onboarding.OnBoarding.M
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

object OnBoardingUpdate : Update<M, E, F>, OnBoardingUpdateSpec {

    override fun update(model: M, event: E) = patch(model, event)

    override fun onSkipClicked(model: M): Next<M, F> {
        return when {
            model.isLoading -> noChange()
            else -> next(
                model.copy(
                    isLoading = true,
                    pendingTarget = M.Target.SKIP
                ),
                setOf(
                    F.CreateWallet,
                    F.TrackEvent(EventUtils.EVENT_SKIP_BUTTON),
                    F.TrackEvent(EventUtils.EVENT_FINAL_PAGE_BROWSE_FIRST)
                )
            )
        }
    }

    override fun onBackClicked(model: M): Next<M, F> {
        return when {
            model.isLoading -> noChange()
            else -> dispatch(
                setOf(
                    F.Cancel,
                    F.TrackEvent(EventUtils.EVENT_BACK_BUTTON)
                )
            )
        }
    }

    override fun onBuyClicked(model: M): Next<M, F> {
        return when {
            model.isLoading -> noChange()
            else -> next(
                model.copy(
                    isLoading = true,
                    pendingTarget = M.Target.BUY
                ),
                setOf(
                    F.CreateWallet,
                    F.TrackEvent(EventUtils.EVENT_FINAL_PAGE_BUY_COIN)
                )
            )
        }
    }

    override fun onBrowseClicked(model: M): Next<M, F> {
        return when {
            model.isLoading -> noChange()
            else -> next(
                model.copy(
                    isLoading = true,
                    pendingTarget = M.Target.BROWSE
                ),
                setOf(
                    F.CreateWallet,
                    F.TrackEvent(EventUtils.EVENT_FINAL_PAGE_BROWSE_FIRST)
                )
            )
        }
    }

    override fun onPageChanged(
        model: M,
        event: E.OnPageChanged
    ): Next<M, F> {
        return next(
            model.copy(page = event.page),
            setOf(
                F.TrackEvent(
                    when (event.page) {
                        1 -> EventUtils.EVENT_GLOBE_PAGE_APPEARED
                        2 -> EventUtils.EVENT_COINS_PAGE_APPEARED
                        3 -> EventUtils.EVENT_FINAL_PAGE_APPEARED
                        else -> error("Invalid page, expected 1-3")
                    }
                )
            )
        )
    }

    override fun onWalletCreated(model: M): Next<M, F> {
        return when (model.pendingTarget) {
            M.Target.NONE -> noChange()
            M.Target.BROWSE -> dispatch(setOf(F.Browse))
            M.Target.BUY -> dispatch(setOf(F.Buy))
            M.Target.SKIP -> dispatch(setOf(F.Skip))
        }
    }

    override fun setupError(
        model: M,
        event: SetupError
    ): Next<M, F> {
        // TODO: For now, we collapse all errors into a
        //  single message to replicate previous behavior.
        val error = when (event) {
            SetupError.PhraseCreationFailed,
            SetupError.StoreWalletFailed,
            SetupError.CryptoSystemBootError ->
                "Failed to generate wallet, please try again."
        }
        return next(
            model.copy(
                isLoading = false,
                pendingTarget = M.Target.NONE
            ),
            setOf(F.ShowError(error))
        )
    }
}
