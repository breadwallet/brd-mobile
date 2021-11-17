/**
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 10/1/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.databinding.ControllerExchangeFeaturePromotionBinding
import com.breadwallet.util.setStatusBarColor

class FeaturePromotionController(args: Bundle? = null) : ExchangeController.ChildController(args) {

    enum class SelectionType {
        BUY, TRADE
    }

    private val selectionType: SelectionType = arg(SelectionType::class.java.simpleName)

    private val binding by viewBinding(ControllerExchangeFeaturePromotionBinding::inflate)

    constructor(target: SelectionType) : this(
        bundleOf(SelectionType::class.java.simpleName to target)
    )

    override fun onCreateView(view: View) {
        super.onCreateView(view)

        with(binding) {
            if (selectionType == SelectionType.BUY) {
                activity?.setStatusBarColor(R.color.primary_button_default)
                root.setBackgroundColor(getColor(R.color.primary_button_default))
                featurePromotionTitle.setText(R.string.Exchange_FeaturePromotion_buyTitle)
                featurePromotionSubtitle.setText(R.string.Exchange_FeaturePromotion_buyBody)
                featurePromotionLogo.setImageResource(R.drawable.feature_promotion_buy)
            } else {
                activity?.setStatusBarColor(R.color.marketing_pink)
                root.setBackgroundColor(getColor(R.color.marketing_pink))
                featurePromotionTitle.setText(R.string.Exchange_FeaturePromotion_tradeTitle)
                featurePromotionSubtitle.setText(R.string.Exchange_FeaturePromotion_tradeBody)
                featurePromotionLogo.setImageResource(R.drawable.feature_promotion_trade)
            }

            buttonContinue.setText(R.string.Exchange_FeaturePromotion_CTA)
            buttonContinue.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnContinueClicked)
            }
        }
    }

    override fun ExchangeModel.render() {}

    override fun onDetach(view: View) {
        super.onDetach(view)
        activity?.setStatusBarColor(android.R.color.darker_gray)
    }
}
