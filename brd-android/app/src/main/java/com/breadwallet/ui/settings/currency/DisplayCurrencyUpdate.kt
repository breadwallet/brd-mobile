/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 1/7/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.currency

import com.breadwallet.ui.settings.currency.DisplayCurrency.E
import com.breadwallet.ui.settings.currency.DisplayCurrency.F
import com.breadwallet.ui.settings.currency.DisplayCurrency.M
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

val CurrencyUpdate = Update<M, E, F> { model, event ->
    when (event) {
        E.OnBackClicked -> dispatch(effects(F.Nav.GoBack))
        E.OnFaqClicked -> dispatch(effects(F.Nav.GoToFaq))
        is E.OnCurrencySelected -> {
            dispatch(effects(F.SetDisplayCurrency(event.currencyCode)))
        }
        is E.OnCurrenciesLoaded -> {
            next(
                model.copy(
                    selectedCurrency = event.selectedCurrencyCode,
                    currencies = event.currencies
                )
            )
        }
        is E.OnSelectedCurrencyUpdated -> {
            next(model.copy(selectedCurrency = event.currencyCode))
        }
        else -> noChange()
    }
}
