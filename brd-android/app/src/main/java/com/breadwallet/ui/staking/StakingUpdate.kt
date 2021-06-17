/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/30/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.staking

import com.breadwallet.breadbox.toBigDecimal
import com.breadwallet.ui.staking.Staking.E
import com.breadwallet.ui.staking.Staking.F
import com.breadwallet.ui.staking.Staking.M
import com.breadwallet.ui.staking.Staking.M.ViewValidator.State.PENDING_STAKE
import com.breadwallet.ui.staking.Staking.M.ViewValidator.State.PENDING_UNSTAKE
import com.breadwallet.ui.staking.Staking.M.ViewValidator.State.STAKED
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

object StakingUpdate : Update<M, E, F> {
    override fun update(model: M, event: E): Next<M, F> = when (event) {
        is E.AccountUpdated -> accountUpdated(model, event)
        is E.OnBakerChanged -> onBakerChanged(model, event)
        is E.OnAddressValidated -> onAddressValidated(model, event)
        is E.OnTransferFailed -> onTransferFailed(model, event)
        is E.OnFeeUpdated -> onFeeUpdated(model, event)
        is E.OnAuthenticationSettingsUpdated -> onAuthenticationSettingsUpdated(model, event)
        is E.OnBakersLoaded -> onBakersLoaded(model, event)
        is E.OnBakerLoaded -> onBakerLoaded(model, event)
        E.OnStakeClicked -> onStakeClicked(model)
        E.OnUnstakeClicked -> onUnstakeClicked(model)
        E.OnCancelClicked -> onCancelClicked(model)
        E.OnConfirmClicked -> onConfirmClicked(model)
        E.OnPasteClicked -> onPasteClicked(model)
        E.OnCloseClicked -> onCloseClicked()
        E.OnHelpClicked -> onHelpClicked()
        E.OnTransactionConfirmClicked -> onTransactionConfirmClicked(model)
        E.OnTransactionCancelClicked -> onTransactionCancelClicked(model)
        E.OnAuthSuccess -> onAuthSuccess(model)
        E.OnAuthCancelled -> onAuthCancelled(model)
        E.OnSelectBakerClicked -> onSelectBakerClicked(model)
        E.OnBakersFailed -> onBakersFailed(model)
        E.OnBakerFailed -> onBakerFailed(model)
    }

    private fun accountUpdated(
        model: M,
        event: E.AccountUpdated
    ): Next<M, F> =
        when (event) {
            is E.AccountUpdated.Unstaked ->
                next(M.SetValidator.createDefault(
                    event.balance,
                    model.currencyId,
                    event.currencyCode,
                    isFingerprintEnabled = model.isFingerprintEnabled,
                    bakers = model.bakers,
                    selectedBaker = (model as? M.SetValidator)?.selectedBaker,
                ))
            is E.AccountUpdated.Staked -> {
                val baker = model.bakers.find { it.address == event.address }
                next(
                    M.ViewValidator(
                        state = event.state,
                        balance = event.balance,
                        address = event.address,
                        currencyCode = event.currencyCode,
                        currencyId = model.currencyId,
                        isFingerprintEnabled = model.isFingerprintEnabled,
                        bakers = model.bakers,
                        baker = baker
                    ),
                    if (baker == null && event.state != PENDING_UNSTAKE) setOf(F.LoadBaker(event.address)) else emptySet()
                )
            }
        }

    private fun onBakerChanged(
        model: M,
        event: E.OnBakerChanged
    ): Next<M, F> {
        val address = event.baker.address
        return if (model.address == address) {
            noChange()
        } else {
            when (model) {
                is M.SetValidator -> {
                    next(
                        model.copy(
                            selectedBaker = event.baker,
                            address = address,
                            isAddressValid = (address.isBlank() || model.isAddressValid) && address != model.originalAddress,
                            isAddressChanged = address != model.originalAddress,
                            canSubmitTransfer = false,
                            feeEstimate = null
                        ),
                        setOfNotNull(
                            if (address.isNotBlank() && !model.isWalletEmpty) {
                                F.ValidateAddress(address)
                            } else null
                        )
                    )
                }
                is M.ViewValidator -> {
                    next(
                        M.SetValidator(
                            balance = model.balance,
                            currencyId = model.currencyId,
                            currencyCode = model.currencyCode,
                            address = event.baker.address,
                            isAddressValid = true,
                            isAddressChanged = false,
                            canSubmitTransfer = false,
                            transactionError = null,
                            isCancellable = model.address.isNotBlank(),
                            originalAddress = model.address,
                            feeEstimate = null,
                            isFingerprintEnabled = model.isFingerprintEnabled,
                            bakers = model.bakers,
                            selectedBaker = event.baker,
                        ),
                        setOfNotNull(
                            if (address.isNotBlank()) {
                                F.ValidateAddress(address)
                            } else null
                        )
                    )
                }
                else -> noChange()
            }
        }
    }

    private fun onAddressValidated(
        model: M,
        event: E.OnAddressValidated
    ): Next<M, F> = when (model) {
        is M.SetValidator -> next(
            model.copy(
                isAddressValid = event.isValid,
                canSubmitTransfer = event.isValid,
            ),
            setOfNotNull(
                if (event.isValid) {
                    F.EstimateFee(model.address)
                } else null
            )
        )
        else -> noChange()
    }

    private fun onTransferFailed(
        model: M,
        event: E.OnTransferFailed
    ): Next<M, F> = when (model) {
        is M.SetValidator -> next(
            model.copy(
                transactionError = event.transactionError,
                confirmWhenReady = false
            )
        )
        else -> noChange()
    }

    private fun onFeeUpdated(
        model: M,
        event: E.OnFeeUpdated
    ): Next<M, F> = when (model) {
        is M.ViewValidator -> if (model.state == PENDING_UNSTAKE) {
            next(
                model.copy(feeEstimate = event.feeEstimate),
                setOf(
                    F.ConfirmTransaction(
                        currencyCode = model.currencyCode,
                        address = null,
                        balance = event.balance,
                        fee = event.feeEstimate.fee.toBigDecimal()
                    )
                )
            )
        } else noChange()
        is M.SetValidator -> if (event.address == model.address) {
            next(model.copy(feeEstimate = event.feeEstimate))
        } else {
            next(model)
        }
        else -> noChange()
    }

    private fun onStakeClicked(model: M): Next<M, F> = when (model) {
        is M.SetValidator -> when {
            model.isAddressValid &&
                model.address.isNotBlank() &&
                model.feeEstimate != null ->
                dispatch(
                    setOf(
                        F.ConfirmTransaction(
                            currencyCode = model.currencyCode,
                            address = model.address,
                            balance = model.balance,
                            fee = model.feeEstimate.fee.toBigDecimal()
                        )
                    )
                )
            model.feeEstimate == null ->
                next(model.copy(confirmWhenReady = true))
            else -> noChange()
        }
        else -> noChange()
    }

    private fun onUnstakeClicked(model: M): Next<M, F> = when (model) {
        is M.ViewValidator -> if (model.state == STAKED) {
            next(
                model.copy(state = PENDING_UNSTAKE),
                setOf(F.EstimateFee(null))
            )
        } else noChange()
        else -> noChange()
    }

    private fun onCancelClicked(model: M): Next<M, F> = when (model) {
        is M.SetValidator -> if (model.isCancellable) {
            next(
                M.ViewValidator(
                    address = model.originalAddress,
                    balance = model.balance,
                    state = STAKED,
                    currencyId = model.currencyId,
                    currencyCode = model.currencyCode,
                    bakers = model.bakers,
                    baker = model.bakers.find { it.address == model.originalAddress }
                )
            )
        } else noChange()
        else -> noChange()
    }

    private fun onConfirmClicked(model: M): Next<M, F> = when (model) {
        is M.SetValidator -> if (
            model.isAddressValid &&
            model.isAddressChanged &&
            model.feeEstimate != null
        ) {
            next(
                M.ViewValidator(
                    currencyId = model.currencyId,
                    currencyCode = model.currencyCode,
                    address = model.address,
                    balance = model.balance,
                    state = PENDING_STAKE,
                    bakers = model.bakers
                ),
                setOf(F.Stake(model.address, model.feeEstimate))
            )
        } else noChange()
        else -> noChange()
    }

    private fun onPasteClicked(model: M): Next<M, F> = when (model) {
        is M.SetValidator -> dispatch(setOf(
            if (model.originalAddress.isNotBlank()) {
                F.PasteFromClipboard(model.originalAddress)
            } else {
                F.PasteFromClipboard(model.address)
            }
        ))
        else -> noChange()
    }

    private fun onCloseClicked(): Next<M, F> =
        dispatch(setOf(F.Close))

    private fun onHelpClicked(): Next<M, F> =
        dispatch(setOf(F.Help))

    private fun onTransactionConfirmClicked(model: M): Next<M, F> =
        when (model) {
            is M.ViewValidator -> next(model.copy(isAuthenticating = true))
            is M.SetValidator -> next(model.copy(isAuthenticating = true))
            else -> noChange()
        }

    private fun onAuthSuccess(model: M): Next<M, F> =
        when (model) {
            is M.SetValidator -> if (
                model.isAddressValid &&
                model.address.isNotBlank() &&
                model.feeEstimate != null
            ) {
                next(
                    model.copy(isAuthenticating = false),
                    setOf(F.Stake(model.address, model.feeEstimate))
                )
            } else noChange()
            is M.ViewValidator -> if (model.state == PENDING_UNSTAKE && model.feeEstimate != null) {
                next(
                    model.copy(isAuthenticating = false),
                    setOf(F.Unstake(model.feeEstimate))
                )
            } else noChange()
            else -> noChange()
        }

    private fun onAuthCancelled(model:M): Next<M, F> =
        when (model) {
            is M.ViewValidator -> next(model.copy(isAuthenticating = false, state = STAKED))
            is M.SetValidator -> next(model.copy(isAuthenticating = false))
            else -> noChange()
        }

    private fun onTransactionCancelClicked(model: M): Next<M, F> =
        when (model) {
            is M.ViewValidator -> if (model.state == PENDING_UNSTAKE) {
                next(model.copy(state = STAKED))
            } else noChange()
            else -> noChange()
        }

    private fun onAuthenticationSettingsUpdated(model: M, event: E.OnAuthenticationSettingsUpdated): Next<M, F> =
        when (model) {
            is M.ViewValidator -> next(model.copy(isFingerprintEnabled = event.isFingerprintEnabled))
            is M.SetValidator -> next(model.copy(isFingerprintEnabled = event.isFingerprintEnabled))
            is M.Loading -> next(model.copy(isFingerprintEnabled = event.isFingerprintEnabled))
        }

    private fun onSelectBakerClicked(model: M): Next<M, F> =
        when (model) {
            is M.SetValidator -> {
                if (model.bakers.isEmpty()) next(model.copy(isLoadingBakers = true), setOf(F.LoadBakers))
                else dispatch(setOf(F.GoToSelectBaker(model.bakers)))
            }
            is M.ViewValidator -> {
                if (model.bakers.isEmpty()) next(model.copy(isLoadingBakers = true), setOf(F.LoadBakers))
                else dispatch(setOf(F.GoToSelectBaker(model.bakers)))
            }
            else -> noChange()
        }

    private fun onBakersLoaded(model: M, event: E.OnBakersLoaded): Next<M,F> =
        next(
            when (model) {
                is M.ViewValidator  -> model.copy(bakers = event.bakers, isLoadingBakers = false)
                is M.SetValidator -> model.copy(bakers = event.bakers, isLoadingBakers = false)
                else -> model
            },
            setOf(F.GoToSelectBaker(event.bakers))
        )

    private fun onBakerLoaded(model: M, event: E.OnBakerLoaded): Next<M,F> =
        next(
            when (model) {
                is M.ViewValidator  -> model.copy(baker = event.baker)
                is M.SetValidator -> model.copy(selectedBaker = event.baker)
                else -> model
            }
        )

    private fun onBakersFailed(model: M): Next<M,F> = next(
        when (model) {
            is M.ViewValidator -> model.copy(isLoadingBakers = false)
            is M.SetValidator -> model.copy(isLoadingBakers = false)
            else -> model
        },
        setOf(F.ShowBakerError)
    )
    private fun onBakerFailed(model: M): Next<M,F> = dispatch(setOf(F.ShowBakerError))
}
