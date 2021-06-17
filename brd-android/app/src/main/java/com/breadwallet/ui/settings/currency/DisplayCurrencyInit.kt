/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 1/7/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.currency

import com.breadwallet.ui.settings.currency.DisplayCurrency.F
import com.breadwallet.ui.settings.currency.DisplayCurrency.M
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

val DisplayCurrencyInit = Init<M, F> { model ->
    first(model, setOf(F.LoadCurrencies))
}
