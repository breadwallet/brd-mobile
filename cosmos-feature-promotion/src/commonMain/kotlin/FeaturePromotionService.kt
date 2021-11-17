/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 9/1/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.featurepromotion

import com.brd.prefs.BrdPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FeaturePromotionService(val preferences: BrdPreferences) {

    private val buyPromotionState =  MutableStateFlow(shouldShowHydraBuy())
    val buyPromotion = buyPromotionState.asStateFlow()

    private val tradePromotionState =  MutableStateFlow(shouldShowHydraTrade())
    val tradePromotion = tradePromotionState.asStateFlow()

    fun shouldShowHydraBuy(): Boolean {
        return !preferences.featurePromotionBuyShown && preferences.hydraActivated
    }

    fun markHydraBuyShown() {
        preferences.featurePromotionBuyShown = true
        buyPromotionState.update { shouldShowHydraBuy() }
    }

    fun shouldShowHydraTrade(): Boolean {
        return !preferences.featurePromotionTradeShown && preferences.hydraActivated
    }

    fun markHydraTradeShown() {
        preferences.featurePromotionTradeShown = true
        tradePromotionState.update { shouldShowHydraTrade() }
    }
}