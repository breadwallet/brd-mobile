/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.onboarding

import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.ui.navigation.OnCompleteAction

object OnBoarding {

    /** Displays informational pages and allows initialization of a wallet. */
    data class M(
        /** The currently selected onboarding page. */
        val page: Int = 1,
        /** True when a wallet is being initialized and navigation must be blocked. */
        val isLoading: Boolean = false,
        /** The user's desired location after the wallet is initialized */
        val pendingTarget: Target = Target.NONE
    ) {
        companion object {
            val DEFAULT = M()
        }

        enum class Target {
            NONE, SKIP, BUY, BROWSE
        }

        val isFirstPage = page == 1
    }

    sealed class E {
        data class OnPageChanged(val page: Int) : E()
        object OnSkipClicked : E()
        object OnBackClicked : E()
        object OnBuyClicked : E()
        object OnBrowseClicked : E()

        object OnWalletCreated : E()

        sealed class SetupError : E() {
            object PhraseCreationFailed : SetupError()

            object StoreWalletFailed : SetupError()

            object CryptoSystemBootError : SetupError()
        }
    }

    sealed class F {
        data class TrackEvent(val event: String) : F()

        object CreateWallet : F()

        data class ShowError(val message: String) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.AlertDialog(
                title = "",
                message = message
            )
        }

        object Skip : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SetPin(onboarding = true)
        }
        object Buy : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SetPin(
                onboarding = true,
                onComplete = OnCompleteAction.GO_TO_BUY
            )
        }
        object Browse : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SetPin(onboarding = true)
        }

        object Cancel : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }
    }
}
