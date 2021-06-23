/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.login

import com.breadwallet.ui.ViewEffect
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import dev.zacsweers.redacted.annotations.Redacted

object LoginScreen {

    data class M(
        val fingerprintEnable: Boolean = false,
        val showHomeScreen: Boolean = true,
        @Redacted val extraUrl: String,
        val isUnlocked: Boolean = false
    ) {
        companion object {
            fun createDefault(
                extraUrl: String,
                showHomeScreen: Boolean
            ) = M(
                extraUrl = extraUrl,
                showHomeScreen = showHomeScreen
            )
        }
    }

    sealed class E {
        object OnFingerprintClicked : E()
        object OnPinLocked : E()
        object OnUnlockAnimationEnd : E()
        data class OnFingerprintEnabled(val enabled: Boolean) : E()

        object OnAuthenticationSuccess : E()
        object OnAuthenticationFailed : E()
    }

    sealed class F {
        object UnlockBrdUser : F()
        object CheckFingerprintEnable : F()
        object AuthenticationSuccess : F(), ViewEffect
        object AuthenticationFailed : F(), ViewEffect
        data class TrackEvent(
            val eventName: String,
            val attributes: Map<String, String>? = null
        ) : F()

        object GoBack : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }
        object GoToHome : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Home
        }
        object ShowFingerprintController : F(), ViewEffect
        object GoToDisableScreen : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.DisabledScreen
        }
        data class GoToDeepLink(
            @Redacted val url: String
        ) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.DeepLink(url, true)
        }
        data class GoToWallet(
            val currencyCode: String
        ) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Wallet(currencyCode)
        }
    }
}
