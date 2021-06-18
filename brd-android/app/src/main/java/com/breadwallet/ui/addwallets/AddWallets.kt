/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 10/11/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.addwallets

import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import dev.zacsweers.redacted.annotations.Redacted

object AddWallets {

    data class M(
        @Redacted val tokens: List<Token> = emptyList(),
        val searchQuery: String = ""
    ) {
        companion object {
            fun createDefault() = M()
        }
    }

    sealed class E {
        data class OnSearchQueryChanged(@Redacted val query: String) : E()
        data class OnTokensChanged(@Redacted val tokens: List<Token>) : E()

        data class OnAddWalletClicked(val token: Token) : E()
        data class OnRemoveWalletClicked(val token: Token) : E()
        object OnBackClicked : E()
    }

    sealed class F {
        data class SearchTokens(@Redacted val query: String) : F()
        data class AddWallet(val token: Token) : F()
        data class RemoveWallet(val token: Token) : F()

        object GoBack : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }
    }
}

data class Token(
    val name: String,
    val currencyCode: String,
    val currencyId: String,
    val startColor: String,
    val enabled: Boolean,
    val removable: Boolean
)
