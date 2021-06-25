/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 1/21/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.writedownkey

import com.breadwallet.tools.util.BRConstants
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.ui.navigation.OnCompleteAction
import dev.zacsweers.redacted.annotations.Redacted

object WriteDownKey {

    data class M(
        val onComplete: OnCompleteAction,
        val requestAuth: Boolean,
        @Redacted val phrase: List<String> = listOf()
    ) {
        companion object {
            fun createDefault(doneAction: OnCompleteAction, requestAuth: Boolean) =
                M(doneAction, requestAuth)
        }
    }

    sealed class E {

        object OnCloseClicked : E()
        object OnFaqClicked : E()
        object OnWriteDownClicked : E()
        object OnGetPhraseFailed : E()
        object OnUserAuthenticated : E()

        data class OnPhraseRecovered(@Redacted val phrase: List<String>) : E()
    }

    sealed class F {
        object GoToFaq : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SupportPage(BRConstants.FAQ_PAPER_KEY)
        }

        object GoToHome : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Home
        }

        object GoBack : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }

        object GoToBuy : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Buy
        }
        object ShowAuthPrompt : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Authentication()
        }
        object GetPhrase : F()

        data class GoToPaperKey(
            @Redacted val phrase: List<String>,
            val onComplete: OnCompleteAction
        ) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.PaperKey(
                phrase,
                onComplete
            )
        }

        data class TrackEvent(
            val eventName: String
        ) : F()
    }
}
