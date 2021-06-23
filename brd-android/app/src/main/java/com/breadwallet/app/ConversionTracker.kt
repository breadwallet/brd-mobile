/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 7/22/2020.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.app

import com.breadwallet.breadbox.BreadBox
import com.breadwallet.tools.manager.BRSharedPrefs
import com.platform.util.AppReviewPromptManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ConversionTracker(
    private val breadBox: BreadBox
) {

    fun start(scope: CoroutineScope) {
        if (!shouldTrackConversions()) return

        BRSharedPrefs.trackedConversionChanges
            .flatMapMerge {
                it.entries.asFlow()
            }
            .flatMapMerge { (currencyCode, trackedConversions) ->
                breadBox.walletTransfers(currencyCode).onEach { transfers ->
                    trackedConversions.forEach { conversion ->
                        val transfer = transfers.find { conversion.isTriggered(it) }
                        if (transfer != null) {
                            BRSharedPrefs.appRatePromptShouldPrompt = true
                            BRSharedPrefs.removeTrackedConversion(conversion)
                        }
                    }
                }
            }
            .launchIn(scope)
    }

    fun track(conversion: Conversion) {
        if (!shouldTrackConversions()) return
        BRSharedPrefs.putTrackedConversion(conversion)
    }

    private fun shouldTrackConversions(): Boolean = AppReviewPromptManager.shouldTrackConversions()
}
