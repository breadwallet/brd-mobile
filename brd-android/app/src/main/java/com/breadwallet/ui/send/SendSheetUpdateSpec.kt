/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.send

import com.spotify.mobius.Next

interface SendSheetUpdateSpec {
    fun patch(model: SendSheet.M, event: SendSheet.E): Next<SendSheet.M, SendSheet.F> = when (event) {
        SendSheet.E.OnNetworkFeeError -> onNetworkFeeError(model)
        SendSheet.E.OnTargetStringEntered -> onTargetStringEntered(model)
        SendSheet.E.GoToEthWallet -> goToEthWallet(model)
        SendSheet.E.OnSendFailed -> onSendFailed(model)
        SendSheet.E.OnSendClicked -> onSendClicked(model)
        SendSheet.E.OnAuthSuccess -> onAuthSuccess(model)
        SendSheet.E.OnAuthCancelled -> onAuthCancelled(model)
        SendSheet.E.OnScanClicked -> onScanClicked(model)
        SendSheet.E.OnFaqClicked -> onFaqClicked(model)
        SendSheet.E.OnCloseClicked -> onCloseClicked(model)
        SendSheet.E.OnPasteClicked -> onPasteClicked(model)
        SendSheet.E.OnAmountEditClicked -> onAmountEditClicked(model)
        SendSheet.E.OnAmountEditDismissed -> onAmountEditDismissed(model)
        SendSheet.E.OnToggleCurrencyClicked -> onToggleCurrencyClicked(model)
        SendSheet.E.OnSendMaxClicked -> onSendMaxClicked(model)
        is SendSheet.E.OnTransferFieldsUpdated -> onTransferFieldsUpdated(model, event)
        is SendSheet.E.OnRequestScanned -> onRequestScanned(model, event)
        is SendSheet.E.OnExchangeRateUpdated -> onExchangeRateUpdated(model, event)
        is SendSheet.E.OnBalanceUpdated -> onBalanceUpdated(model, event)
        is SendSheet.E.OnNetworkFeeUpdated -> onNetworkFeeUpdated(model, event)
        is SendSheet.E.OnTransferSpeedChanged -> onTransferSpeedChanged(model, event)
        is SendSheet.E.OnTargetStringChanged -> onTargetStringChanged(model, event)
        is SendSheet.E.OnMemoChanged -> onMemoChanged(model, event)
        is SendSheet.E.OnSendComplete -> onSendComplete(model, event)
        is SendSheet.E.OnAuthenticationSettingsUpdated -> onAuthenticationSettingsUpdated(model, event)
        is SendSheet.E.TransferFieldUpdate -> transferFieldUpdate(model, event)
        is SendSheet.E.OnAmountChange -> onAmountChange(model, event)
        is SendSheet.E.ConfirmTx -> confirmTx(model, event)
        is SendSheet.E.OnAddressValidated -> onAddressValidated(model, event)
        is SendSheet.E.PaymentProtocol -> paymentProtocol(model, event)
        is SendSheet.E.OnMaxEstimated -> onMaxEstimated(model, event)
        SendSheet.E.OnMaxEstimateFailed -> onMaxEstimateFailed(model)
    }

    fun onSendMaxClicked(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onMaxEstimated(model: SendSheet.M, event: SendSheet.E.OnMaxEstimated): Next<SendSheet.M, SendSheet.F>

    fun onMaxEstimateFailed(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onNetworkFeeError(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onTargetStringEntered(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun goToEthWallet(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onSendFailed(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onSendClicked(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onAuthSuccess(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onAuthCancelled(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onScanClicked(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onFaqClicked(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onCloseClicked(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onPasteClicked(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onAmountEditClicked(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onAmountEditDismissed(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onToggleCurrencyClicked(model: SendSheet.M): Next<SendSheet.M, SendSheet.F>

    fun onTransferFieldsUpdated(model: SendSheet.M, event: SendSheet.E.OnTransferFieldsUpdated): Next<SendSheet.M, SendSheet.F>

    fun onRequestScanned(model: SendSheet.M, event: SendSheet.E.OnRequestScanned): Next<SendSheet.M, SendSheet.F>

    fun onExchangeRateUpdated(model: SendSheet.M, event: SendSheet.E.OnExchangeRateUpdated): Next<SendSheet.M, SendSheet.F>

    fun onBalanceUpdated(model: SendSheet.M, event: SendSheet.E.OnBalanceUpdated): Next<SendSheet.M, SendSheet.F>

    fun onNetworkFeeUpdated(model: SendSheet.M, event: SendSheet.E.OnNetworkFeeUpdated): Next<SendSheet.M, SendSheet.F>

    fun onTransferSpeedChanged(model: SendSheet.M, event: SendSheet.E.OnTransferSpeedChanged): Next<SendSheet.M, SendSheet.F>

    fun onTargetStringChanged(model: SendSheet.M, event: SendSheet.E.OnTargetStringChanged): Next<SendSheet.M, SendSheet.F>

    fun onMemoChanged(model: SendSheet.M, event: SendSheet.E.OnMemoChanged): Next<SendSheet.M, SendSheet.F>

    fun onSendComplete(model: SendSheet.M, event: SendSheet.E.OnSendComplete): Next<SendSheet.M, SendSheet.F>

    fun onAuthenticationSettingsUpdated(model: SendSheet.M, event: SendSheet.E.OnAuthenticationSettingsUpdated): Next<SendSheet.M, SendSheet.F>

    fun transferFieldUpdate(model: SendSheet.M, event: SendSheet.E.TransferFieldUpdate): Next<SendSheet.M, SendSheet.F>

    fun onAmountChange(model: SendSheet.M, event: SendSheet.E.OnAmountChange): Next<SendSheet.M, SendSheet.F>

    fun confirmTx(model: SendSheet.M, event: SendSheet.E.ConfirmTx): Next<SendSheet.M, SendSheet.F>

    fun onAddressValidated(model: SendSheet.M, event: SendSheet.E.OnAddressValidated): Next<SendSheet.M, SendSheet.F>

    fun paymentProtocol(model: SendSheet.M, event: SendSheet.E.PaymentProtocol): Next<SendSheet.M, SendSheet.F>
}
