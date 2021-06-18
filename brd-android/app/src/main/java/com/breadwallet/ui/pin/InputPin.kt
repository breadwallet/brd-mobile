/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 9/23/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.pin

import com.breadwallet.tools.util.BRConstants
import com.breadwallet.ui.ViewEffect
import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.ui.navigation.OnCompleteAction
import dev.zacsweers.redacted.annotations.Redacted

private const val PIN_LENGTH = 6

object InputPin {

    data class M(
        val mode: Mode = Mode.NEW,
        @Redacted val pin: String = "",
        @Redacted val pinConfirmation: String = "",
        val pinUpdateMode: Boolean = false,
        val skipWriteDownKey: Boolean = false,
        val onComplete: OnCompleteAction
    ) {

        companion object {
            fun createDefault(
                pinUpdateMode: Boolean,
                onComplete: OnCompleteAction,
                skipWriteDownKey: Boolean
            ) = M(
                pinUpdateMode = pinUpdateMode,
                onComplete = onComplete,
                skipWriteDownKey = skipWriteDownKey
            )
        }

        enum class Mode {
            VERIFY,  // Verify the old pin
            NEW,     // Chose a new pin
            CONFIRM  // Confirm the new pin
        }
    }

    sealed class E {

        object OnFaqClicked : E()
        object OnPinLocked : E()
        object OnPinSaved : E()
        object OnPinSaveFailed : E()

        data class OnPinEntered(
            @Redacted val pin: String,
            val isPinCorrect: Boolean
        ) : E()

        data class OnPinCheck(
            val hasPin: Boolean
        ) : E()
    }

    sealed class F {

        object CheckIfPinExists : F()
        data class SetupPin(
            @Redacted val pin: String
        ) : F() {
            init {
                require(pin.length == PIN_LENGTH) {
                    "pin must contain $PIN_LENGTH digits"
                }
            }
        }

        object ErrorShake : F(), ViewEffect
        object ShowPinError : F(), ViewEffect

        object GoToHome : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Home
        }
        object GoToFaq : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.SupportPage(BRConstants.FAQ_SET_PIN)
        }
        object GoToDisabledScreen : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.DisabledScreen
        }

        data class GoToWriteDownKey(val onComplete: OnCompleteAction) : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.WriteDownKey(onComplete, false)
        }

        data class TrackEvent(val event: String) : F()
    }
}
