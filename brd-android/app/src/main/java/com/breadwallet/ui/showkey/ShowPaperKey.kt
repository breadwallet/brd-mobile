/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.showkey

import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.ui.navigation.OnCompleteAction
import dev.zacsweers.redacted.annotations.Redacted

object ShowPaperKey {
    data class M(
        @Redacted val phrase: List<String>,
        val onComplete: OnCompleteAction?,
        val currentWord: Int = 0,
        val phraseWroteDown: Boolean = false
    ) {
        companion object {
            fun createDefault(
                phrase: List<String>,
                onComplete: OnCompleteAction?,
                phraseWroteDown: Boolean
            ) = M(phrase, onComplete, phraseWroteDown = phraseWroteDown)
        }
    }

    sealed class E {

        object OnNextClicked : E()
        object OnPreviousClicked : E()
        object OnCloseClicked : E()

        data class OnPageChanged(val position: Int) : E()
    }

    sealed class F {

        object GoToHome : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Home
        }
        object GoToBuy : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Buy
        }
        object GoBack : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }
        data class GoToPaperKeyProve(
            @Redacted val phrase: List<String>,
            val onComplete: OnCompleteAction
        ) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.PaperKeyProve(
                phrase,
                onComplete
            )
        }
    }
}
