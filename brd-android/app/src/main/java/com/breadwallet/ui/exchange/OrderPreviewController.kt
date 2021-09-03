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
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader.TileMode
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.brd.api.models.ExchangeInvoiceEstimate
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.databinding.ControllerExchangeOrderPreviewBinding
import com.breadwallet.tools.util.TokenUtil
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

class OrderPreviewController(args: Bundle? = null) : ExchangeController.ChildController(args) {

    private val binding by viewBinding(ControllerExchangeOrderPreviewBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        with(binding) {
            receiptLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            buttonBack.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnBackClicked)
            }
            buttonContinue.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnContinueClicked)
            }
            var expanded = false
            buttonExpandFees.setOnClickListener {
                expanded = !expanded
                groupNetworkFee.isVisible = expanded && labelNetworkFeeValue.text.isNotBlank()
                groupExchangeFee.isVisible = expanded && labelExchangeFeeValue.text.isNotBlank()
                groupRate.isVisible = expanded
                iconFeeExpandArrow.rotation = if (expanded) 270f else 90f
            }

            labelBrdRewards.apply {
                val colors = intArrayOf(
                    getColor(R.color.logo_gradient_end),
                    getColor(R.color.logo_gradient_start),
                )
                val textWidth = paint.measureText(text.toString())
                paint.shader = LinearGradient(0f, 0f, textWidth, textSize, colors, null, TileMode.REPEAT)
            }
        }
    }

    override fun ExchangeModel.render() {
        val offerDetails = (selectedOffer as? ExchangeModel.OfferDetails.ValidOffer) ?: return
        with(binding) {
            root.isVisible = true
            labelSourceCurrencyCode.text = sourceCurrencyCode?.toUpperCase(Locale.ROOT)
            labelQuoteCurrencyCode.text = quoteCurrencyCode?.toUpperCase(Locale.ROOT)
            labelFromAmountValue.text = offerDetails.formattedSourceTotal
            labelToValue.text = offerDetails.formattedQuoteTotal
            labelFeesValue.text = offerDetails.run { formattedQuoteFees ?: formattedSourceFees }
            labelNetworkFeeValue.text = offerDetails.formattedNetworkFee
            labelExchangeFeeValue.text = offerDetails.formattedProviderFee
            labelRateValue.text = offerDetails.formattedSourceRatePerQuote
            labelMethodValue.text = offerDetails.offer.provider.name
            labelDeliveryValue.text = offerDetails.offer.deliveryEstimate

            val brdDiscount = offerDetails.offer.discounts.find {
                it.type == ExchangeInvoiceEstimate.DiscountType.PLATFORM
            }
            if (brdDiscount == null) {
                labelExchangeFeeDiscount.isVisible = false
                labelBrdRewardsDetails.isVisible = true
                labelBrdRewardsStatus.setTextColor(getColor(R.color.ui_error))
                labelBrdRewardsStatus.setText(R.string.Exchange_Preview_inactive)
            } else {
                labelExchangeFeeDiscount.isVisible = true
                labelBrdRewardsDetails.isVisible = false
                labelBrdRewardsStatus.setTextColor(Color.parseColor("#41BB85"))
                labelBrdRewardsStatus.setText(R.string.Exchange_Preview_active)
            }

            val picasso = Picasso.get()
            sourceCurrencyCode?.also { sourceCurrencyCode ->
                TokenUtil.getTokenStartColor(sourceCurrencyCode)?.run(Color::parseColor)?.also { tokenColor ->
                    labelFromAmountValue.setTextColor(tokenColor)
                    layoutSourceCurrencyColor.imageTintList = ColorStateList.valueOf(tokenColor)
                }
                TokenUtil.getTokenIconPath(sourceCurrencyCode, false)?.also { sourceIcon ->
                    picasso.load(File(sourceIcon)).into(imageSourceCurrencyIcon)
                }
            }
            quoteCurrencyCode?.also { quoteCurrencyCode ->
                TokenUtil.getTokenStartColor(quoteCurrencyCode)?.run(Color::parseColor)?.also { tokenColor ->
                    labelToValue.setTextColor(tokenColor)
                    layoutQuoteCurrencyColor.imageTintList = ColorStateList.valueOf(tokenColor)
                }
                TokenUtil.getTokenIconPath(quoteCurrencyCode, false)?.also { quoteIcon ->
                    picasso.load(File(quoteIcon)).into(imageQuoteCurrencyIcon)
                }
            }
        }
    }
}
