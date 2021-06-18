/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/12/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
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

