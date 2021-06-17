/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 1/7/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.currency

import com.breadwallet.tools.util.BRConstants
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget

object DisplayCurrency {

    data class M(val currencies: List<String>, val selectedCurrency: String) {
        companion object {
            fun createDefault(): M = M(emptyList(), "")
        }
    }

    sealed class E {
        object OnBackClicked : E()
        object OnFaqClicked : E()
        data class OnCurrencySelected(val currencyCode: String) : E()
        data class OnCurrenciesLoaded(
            val selectedCurrencyCode: String,
            val currencies: List<String>
        ) : E()

        data class OnSelectedCurrencyUpdated(val currencyCode: String) : E()
    }

    sealed class F {
        object LoadCurrencies : F()
        data class SetDisplayCurrency(val currencyCode: String) : F()
        sealed class Nav(
            override val navigationTarget: NavigationTarget
        ) : F(), NavigationEffect {
            object GoBack : Nav(NavigationTarget.Back)
            object GoToFaq : Nav(
                NavigationTarget.SupportPage(BRConstants.FAQ_DISPLAY_CURRENCY)
            )
        }
    }
}
