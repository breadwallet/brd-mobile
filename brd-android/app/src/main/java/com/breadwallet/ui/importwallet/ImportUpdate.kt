/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 12/3/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.importwallet

import com.breadwallet.breadbox.toBigDecimal
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.importwallet.Import.E
import com.breadwallet.ui.importwallet.Import.F
import com.breadwallet.ui.importwallet.Import.M
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

val ImportUpdate = Update<M, E, F> { model, event ->
    when (event) {
        is E.OnScanClicked -> dispatch(setOf(F.Nav.GoToScan))
        E.OnFaqClicked -> dispatch(setOf(F.Nav.GoToFaq))
        E.OnCloseClicked -> when (model.loadingState) {
            M.LoadingState.IDLE -> dispatch(setOf<F>(F.Nav.GoBack))
            else -> noChange()
        }
        E.Key.NoWallets -> dispatch(setOf(F.Nav.GoBack))
        is E.Key.OnValid -> when {
            event.isPasswordProtected -> when {
                !model.keyPassword.isNullOrEmpty() -> next(
                    model.copy(
                        isKeyValid = true,
                        keyRequiresPassword = true
                    ),
                    setOf<F>(
                        F.EstimateImport.KeyWithPassword(
                            privateKey = checkNotNull(model.privateKey),
                            password = model.keyPassword
                        )
                    )
                )
                else -> next(
                    model.copy(
                        isKeyValid = true,
                        keyRequiresPassword = true
                    ),
                    setOf<F>(F.ShowPasswordInput)
                )
            }
            else -> next(
                model.copy(
                    isKeyValid = true,
                    keyRequiresPassword = false
                ),
                setOf<F>(
                    F.EstimateImport.Key(
                        privateKey = checkNotNull(model.privateKey)
                    )
                )
            )
        }
        E.Key.OnInvalid -> next(
            model.reset(),
            setOf(F.ShowKeyInvalid)
        )
        E.Key.OnPasswordInvalid -> next(
            model.reset(),
            setOf(F.ShowPasswordInvalid)
        )
        is E.OnKeyScanned -> next(
            model.copy(
                privateKey = event.privateKey,
                keyRequiresPassword = event.isPasswordProtected,
                isKeyValid = true,
                loadingState = M.LoadingState.VALIDATING
            ),
            setOf(
                if (event.isPasswordProtected) {
                    F.ShowPasswordInput
                } else {
                    F.ValidateKey(event.privateKey, null)
                }
            )
        )
        is E.RetryImport -> {
            val newModel = model.copy(
                privateKey = event.privateKey,
                keyPassword = event.password,
                keyRequiresPassword = event.password != null,
                isKeyValid = true,
                loadingState = M.LoadingState.ESTIMATING
            )
            val estimateEffect = when {
                newModel.keyRequiresPassword ->
                    F.EstimateImport.KeyWithPassword(
                        privateKey = event.privateKey,
                        password = checkNotNull(event.password)
                    )
                else ->
                    F.EstimateImport.Key(
                        privateKey = event.privateKey
                    )
            }
            next(model, setOf<F>(estimateEffect))
        }
        is E.Estimate.Success -> {
            val balance = event.balance.toBigDecimal()
            val fee = event.feeAmount.toBigDecimal()
            next(
                model.copy(
                    currencyCode = event.currencyCode
                ),
                setOf(
                    F.ShowConfirmImport(
                        receiveAmount = (balance - fee).toPlainString(),
                        feeAmount = fee.toPlainString()
                    )
                )
            )
        }
        is E.Estimate.FeeError ->
            next(model.reset(), setOf(F.ShowImportFailed))
        is E.Estimate.BalanceTooLow ->
            next(model.reset(), setOf(F.ShowBalanceTooLow))
        E.Estimate.NoBalance ->
            next(model.reset(), setOf(F.ShowNoBalance))
        is E.Transfer.OnSuccess -> {
            val effects = mutableSetOf<F>(F.ShowImportSuccess)
            if (model.gift) {
                effects.add(F.TrackEvent(EventUtils.EVENT_GIFT_REDEEM))
                if (model.scanned) {
                    effects.add(F.TrackEvent(EventUtils.EVENT_GIFT_REDEEM_SCAN))
                } else {
                    effects.add(F.TrackEvent(EventUtils.EVENT_GIFT_REDEEM_LINK))
                }
            } else if (!model.reclaimGiftHash.isNullOrBlank()) {
                effects.add(F.TrackEvent(EventUtils.EVENT_GIFT_REDEEM))
                effects.add(F.TrackEvent(EventUtils.EVENT_GIFT_REDEEM_RECLAIM))
            }

            next(
                model.reset(),
                effects
            )
        }
        E.Transfer.OnFailed ->
            next(model.reset(), setOf(F.ShowImportFailed))
        E.OnImportCancel -> next(model.reset())
        E.OnImportConfirm ->
            next(
                model.copy(
                    loadingState = M.LoadingState.SUBMITTING
                ),
                setOf(
                    F.SubmitImport(
                        privateKey = checkNotNull(model.privateKey),
                        password = model.keyPassword,
                        currencyCode = checkNotNull(model.currencyCode),
                        reclaimGiftHash = model.reclaimGiftHash
                    )
                )
            )
        is E.OnPasswordEntered -> when {
            model.privateKey != null && model.keyRequiresPassword ->
                next(
                    model.copy(
                        keyPassword = event.password,
                        loadingState = M.LoadingState.VALIDATING
                    ),
                    setOf<F>(
                        F.ValidateKey(
                            privateKey = model.privateKey,
                            password = event.password
                        )
                    )
                )
            else -> noChange()
        }
    }
}
