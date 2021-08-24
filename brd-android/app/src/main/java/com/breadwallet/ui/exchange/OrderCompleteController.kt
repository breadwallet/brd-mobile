/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.databinding.ControllerExchangeOrderCompleteBinding
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.ui.navigation.RouterNavigator
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

private const val CONFETTI_DURATION = 4_000L

class OrderCompleteController(args: Bundle? = null) : ExchangeController.ChildController(args) {

    private val binding by viewBinding(ControllerExchangeOrderCompleteBinding::inflate)
    private val routerNavigator by lazy {
        RouterNavigator { checkNotNull(parentController).router }
    }

    private var hasPlayedConfetti = false

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        with(binding) {
            receiptLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            buttonContinue.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnContinueClicked)
            }
            var expanded = false
            buttonExpandFees.setOnClickListener {
                expanded = !expanded
                groupNetworkFee.isVisible = expanded && labelNetworkFeeValue.text.isNotBlank()
                groupProviderFee.isVisible = expanded && labelProviderFeeValue.text.isNotBlank()
                groupPlatformFee.isVisible = expanded && labelPlatformFeeValue.text.isNotBlank()
                iconFeeExpandArrow.rotation = if (expanded) 270f else 90f
            }
            buttonReceipt.setOnClickListener {
                val state = (currentModel.state as? ExchangeModel.State.OrderComplete)
                    ?: return@setOnClickListener
                routerNavigator.orderHistory(NavigationTarget.OrderHistory(state.order.orderId))
            }
        }
    }

    override fun ExchangeModel.render() {
        val state = (state as? ExchangeModel.State.OrderComplete) ?: return
        with(binding) {
            root.isVisible = true
            labelPurchaseAmountValue.text = state.offerDetails.formattedQuoteTotal
            labelPurchaseCostValue.text = state.offerDetails.formattedSourceTotal
            labelFeesValue.text =
                state.offerDetails.run { formattedSourceFees ?: formattedQuoteFees }
            labelNetworkFeeValue.text = state.offerDetails.formattedNetworkFee
            labelProviderFeeValue.text = state.offerDetails.formattedProviderFee
            labelPlatformFeeValue.text = state.offerDetails.formattedPlatformFee
            labelMethodValue.text =
                "${state.order.provider.name} by ${state.offerDetails.offer.sourceCurrencyMethod::class.simpleName}"
            labelDeliveryValue.text = state.offerDetails.offer.deliveryEstimate

            if (!hasPlayedConfetti) {
                hasPlayedConfetti = true
                viewAttachScope.launch(Main) {
                    confetti.translationY = -confetti.drawable.intrinsicHeight.toFloat()
                    confetti.isVisible = true
                    ViewCompat.animate(confetti)
                        .translationY(layoutHeader.measuredHeight.toFloat())
                        .setDuration(CONFETTI_DURATION)
                        .setInterpolator(LinearInterpolator())
                        .withEndAction {
                            if (isAttached) {
                                confetti.isVisible = false
                            }
                        }
                }
            }
        }
    }
}
