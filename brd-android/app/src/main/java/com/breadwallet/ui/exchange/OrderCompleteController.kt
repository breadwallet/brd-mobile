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
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.databinding.ControllerExchangeOrderCompleteBinding
import com.breadwallet.ui.navigation.NavigationTarget
import com.breadwallet.ui.navigation.RouterNavigator
import com.breadwallet.util.isDoge
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.media.MediaPlayer
import com.breadwallet.R

private const val CONFETTI_DURATION = 4_800L

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
                val isDoge = state.order.outputs.first().currency.code.isDoge()
                hasPlayedConfetti = true
                viewAttachScope.launch(Main) {
                    delay(200L) // slight delay to start animation
                    confettiContainerLayerOne.translationY =
                        -confetti.drawable.intrinsicHeight.toFloat()
                    confettiContainerLayerTwo.translationY =
                        -confetti.drawable.intrinsicHeight.toFloat()
                    confettiContainerLayerOne.isVisible = true
                    confettiContainerLayerTwo.isVisible = true
                    ViewCompat.animate(confettiContainerLayerOne)
                        .translationY(root.height + layoutHeader.measuredHeight.toFloat())
                        .setDuration(CONFETTI_DURATION)
                        .setInterpolator(LinearInterpolator())
                        .withEndAction {
                            if (isAttached) {
                                confettiContainerLayerOne.isVisible = false
                            }
                        }

                    if (isDoge) {
                        animateDogeItems()
                    }

                    delay(400L)
                    ViewCompat.animate(confettiContainerLayerTwo)
                        .translationY(root.height + layoutHeader.measuredHeight.toFloat())
                        .setDuration(3_800)
                        .setInterpolator(LinearInterpolator())
                        .withEndAction {
                            if (isAttached) {
                                confettiContainerLayerTwo.isVisible = false
                            }
                        }

                    delay(700L)
                    if (isDoge) {
                        val ring: MediaPlayer = MediaPlayer.create(requireContext(), R.raw.bark)
                        ring.start()
                    }
                }
            }
        }
    }

    /** Handle Doge specific animations */
    private fun ControllerExchangeOrderCompleteBinding.animateDogeItems() {
        dogeContainer.translationY = -confetti.drawable.intrinsicHeight.toFloat()
        dogeContainer.isVisible = true
        ViewCompat.animate(dogeContainer)
            .translationY(root.height + layoutHeader.measuredHeight.toFloat())
            .setDuration(4300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                if (isAttached) {
                    dogeContainer.isVisible = false
                }
            }

        ObjectAnimator.ofFloat(dogeOne, "rotation", -45f).apply {
            duration = 3000
            start()
        }

        ObjectAnimator.ofFloat(dogeTwo, "rotation", -160f).apply {
            duration = 3000
            start()
        }

        ObjectAnimator.ofFloat(dogeThree, "rotation", 280f).apply {
            duration = 3500
            start()
        }

        ObjectAnimator.ofFloat(dogeFour, "rotation", -220f).apply {
            duration = 3500
            start()
        }

        dogeWordGroup.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(muchWow, "rotation", -225f).apply {
            duration = 3000
            interpolator = DecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(suchCool, "rotation", 225f).apply {
            duration = 4000
            interpolator = DecelerateInterpolator()
            start()
        }
    }
}
