/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/14/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.wallet

import com.spotify.mobius.Next

interface WalletScreenUpdateSpec {
    fun patch(model: WalletScreen.M, event: WalletScreen.E): Next<WalletScreen.M, WalletScreen.F> = when (event) {
        WalletScreen.E.OnFilterSentClicked -> onFilterSentClicked(model)
        WalletScreen.E.OnFilterReceivedClicked -> onFilterReceivedClicked(model)
        WalletScreen.E.OnFilterPendingClicked -> onFilterPendingClicked(model)
        WalletScreen.E.OnFilterCompleteClicked -> onFilterCompleteClicked(model)
        WalletScreen.E.OnSearchClicked -> onSearchClicked(model)
        WalletScreen.E.OnSearchDismissClicked -> onSearchDismissClicked(model)
        WalletScreen.E.OnBackClicked -> onBackClicked(model)
        WalletScreen.E.OnChangeDisplayCurrencyClicked -> onChangeDisplayCurrencyClicked(model)
        WalletScreen.E.OnSendClicked -> onSendClicked(model)
        WalletScreen.E.OnReceiveClicked -> onReceiveClicked(model)
        WalletScreen.E.OnBrdRewardsClicked -> onBrdRewardsClicked(model)
        WalletScreen.E.OnChartDataPointReleased -> onChartDataPointReleased(model)
        WalletScreen.E.OnCreateAccountClicked -> onCreateAccountClicked(model)
        WalletScreen.E.OnCreateAccountConfirmationClicked -> onCreateAccountConfirmationClicked(model)
        WalletScreen.E.OnStakingCellClicked -> onStakingCellClicked(model)
        is WalletScreen.E.OnSyncProgressUpdated -> onSyncProgressUpdated(model, event)
        is WalletScreen.E.OnQueryChanged -> onQueryChanged(model, event)
        is WalletScreen.E.OnCurrencyNameUpdated -> onCurrencyNameUpdated(model, event)
        is WalletScreen.E.OnBrdRewardsUpdated -> onBrdRewardsUpdated(model, event)
        is WalletScreen.E.OnBalanceUpdated -> onBalanceUpdated(model, event)
        is WalletScreen.E.OnFiatPricePerUpdated -> onFiatPricePerUpdated(model, event)
        is WalletScreen.E.OnTransactionsUpdated -> onTransactionsUpdated(model, event)
        is WalletScreen.E.OnTransactionMetaDataUpdated -> onTransactionMetaDataUpdated(model, event)
        is WalletScreen.E.OnTransactionMetaDataLoaded -> onTransactionMetaDataLoaded(model, event)
        is WalletScreen.E.OnVisibleTransactionsChanged -> onVisibleTransactionsChanged(model, event)
        is WalletScreen.E.OnCryptoTransactionsUpdated -> onCryptoTransactionsUpdated(model, event)
        is WalletScreen.E.OnTransactionAdded -> onTransactionAdded(model, event)
        is WalletScreen.E.OnTransactionRemoved -> onTransactionRemoved(model, event)
        is WalletScreen.E.OnConnectionUpdated -> onConnectionUpdated(model, event)
        is WalletScreen.E.OnSendRequestGiven -> onSendRequestGiven(model, event)
        is WalletScreen.E.OnTransactionClicked -> onTransactionClicked(model, event)
        is WalletScreen.E.OnIsCryptoPreferredLoaded -> onIsCryptoPreferredLoaded(model, event)
        is WalletScreen.E.OnChartIntervalSelected -> onChartIntervalSelected(model, event)
        is WalletScreen.E.OnMarketChartDataUpdated -> onMarketChartDataUpdated(model, event)
        is WalletScreen.E.OnChartDataPointSelected -> onChartDataPointSelected(model, event)
        is WalletScreen.E.OnIsTokenSupportedUpdated -> onIsTokenSupportedUpdated(model, event)
        is WalletScreen.E.OnWalletStateUpdated -> onWalletStateUpdated(model, event)
        is WalletScreen.E.OnMarketDataUpdated -> onMarketDataUpdated(model, event)
        is WalletScreen.E.OnGiftClicked -> onGiftClicked(model, event)
    }

    fun onFilterSentClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onFilterReceivedClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onFilterPendingClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onFilterCompleteClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onSearchClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onSearchDismissClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onBackClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onChangeDisplayCurrencyClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onSendClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onReceiveClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onBrdRewardsClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onChartDataPointReleased(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onCreateAccountClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onCreateAccountConfirmationClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onStakingCellClicked(model: WalletScreen.M): Next<WalletScreen.M, WalletScreen.F>

    fun onSyncProgressUpdated(model: WalletScreen.M, event: WalletScreen.E.OnSyncProgressUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onQueryChanged(model: WalletScreen.M, event: WalletScreen.E.OnQueryChanged): Next<WalletScreen.M, WalletScreen.F>

    fun onCurrencyNameUpdated(model: WalletScreen.M, event: WalletScreen.E.OnCurrencyNameUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onBrdRewardsUpdated(model: WalletScreen.M, event: WalletScreen.E.OnBrdRewardsUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onBalanceUpdated(model: WalletScreen.M, event: WalletScreen.E.OnBalanceUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onFiatPricePerUpdated(model: WalletScreen.M, event: WalletScreen.E.OnFiatPricePerUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onTransactionsUpdated(model: WalletScreen.M, event: WalletScreen.E.OnTransactionsUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onTransactionMetaDataUpdated(model: WalletScreen.M, event: WalletScreen.E.OnTransactionMetaDataUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onTransactionMetaDataLoaded(model: WalletScreen.M, event: WalletScreen.E.OnTransactionMetaDataLoaded): Next<WalletScreen.M, WalletScreen.F>

    fun onVisibleTransactionsChanged(model: WalletScreen.M, event: WalletScreen.E.OnVisibleTransactionsChanged): Next<WalletScreen.M, WalletScreen.F>

    fun onCryptoTransactionsUpdated(model: WalletScreen.M, event: WalletScreen.E.OnCryptoTransactionsUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onTransactionAdded(model: WalletScreen.M, event: WalletScreen.E.OnTransactionAdded): Next<WalletScreen.M, WalletScreen.F>

    fun onTransactionRemoved(model: WalletScreen.M, event: WalletScreen.E.OnTransactionRemoved): Next<WalletScreen.M, WalletScreen.F>

    fun onConnectionUpdated(model: WalletScreen.M, event: WalletScreen.E.OnConnectionUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onSendRequestGiven(model: WalletScreen.M, event: WalletScreen.E.OnSendRequestGiven): Next<WalletScreen.M, WalletScreen.F>

    fun onTransactionClicked(model: WalletScreen.M, event: WalletScreen.E.OnTransactionClicked): Next<WalletScreen.M, WalletScreen.F>

    fun onIsCryptoPreferredLoaded(model: WalletScreen.M, event: WalletScreen.E.OnIsCryptoPreferredLoaded): Next<WalletScreen.M, WalletScreen.F>

    fun onChartIntervalSelected(model: WalletScreen.M, event: WalletScreen.E.OnChartIntervalSelected): Next<WalletScreen.M, WalletScreen.F>

    fun onMarketChartDataUpdated(model: WalletScreen.M, event: WalletScreen.E.OnMarketChartDataUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onChartDataPointSelected(model: WalletScreen.M, event: WalletScreen.E.OnChartDataPointSelected): Next<WalletScreen.M, WalletScreen.F>

    fun onIsTokenSupportedUpdated(model: WalletScreen.M, event: WalletScreen.E.OnIsTokenSupportedUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onWalletStateUpdated(model: WalletScreen.M, event: WalletScreen.E.OnWalletStateUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onMarketDataUpdated(model: WalletScreen.M, event: WalletScreen.E.OnMarketDataUpdated): Next<WalletScreen.M, WalletScreen.F>

    fun onGiftClicked(model: WalletScreen.M, event: WalletScreen.E.OnGiftClicked): Next<WalletScreen.M, WalletScreen.F>
}
