/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2019 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.breadwallet.ui.onboarding

import com.breadwallet.logger.logError
import com.breadwallet.logger.logInfo
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.security.SetupResult
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.onboarding.OnBoarding.E
import com.breadwallet.ui.onboarding.OnBoarding.F
import drewcarlson.mobius.flow.subtypeEffectHandler

fun createOnBoardingHandler(
    userManager: BrdUserManager
) = subtypeEffectHandler<F, E> {
    addConsumer<F.TrackEvent> { effect ->
        EventUtils.pushEvent(effect.event)
    }

    addFunction<F.CreateWallet> {
        when (val result = userManager.setupWithGeneratedPhrase()) {
            SetupResult.Success -> {
                logInfo("Wallet created successfully.")
                E.OnWalletCreated
            }
            is SetupResult.FailedToGeneratePhrase -> {
                logError("Failed to generate phrase.", result.exception)
                E.SetupError.PhraseCreationFailed
            }
            else -> {
                // TODO: Handle specific errors, message possible recourse to the user
                logError("Error creating wallet: $result")
                E.SetupError.StoreWalletFailed
            }
        }
    }
}

