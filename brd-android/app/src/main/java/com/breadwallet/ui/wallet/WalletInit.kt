/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 7/26/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.wallet

import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.wallet.WalletScreen.F
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

val WalletInit = Init<WalletScreen.M, F> { model ->
    val effects = effects(
        F.LoadWalletState(model.currencyCode),
        F.LoadWalletBalance(model.currencyCode),
        F.LoadFiatPricePerUnit(model.currencyCode),
        F.LoadCryptoPreferred,
        F.LoadCurrencyName(model.currencyCode),
        F.LoadSyncState(model.currencyCode),
        F.LoadChartInterval(model.priceChartInterval, model.currencyCode),
        F.LoadMarketData(model.currencyCode),
        F.TrackEvent(
            EventUtils.getEventNameWithCurrencyCode(
                EventUtils.EVENT_WALLET_APPEARED,
                model.currencyCode
            )
        ),
        F.LoadIsTokenSupported(model.currencyCode),
        F.LoadTransactions(model.currencyCode),
        F.LoadConnectivityState
    )
    first(model, effects)
}
