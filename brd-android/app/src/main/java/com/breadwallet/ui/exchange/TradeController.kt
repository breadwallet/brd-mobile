/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.databinding.ControllerExchangeTradeBinding
import com.breadwallet.databinding.LayoutTradeAssetBinding
import com.breadwallet.tools.util.TokenUtil
import com.squareup.picasso.Picasso
import java.io.File
import java.util.Locale

private const val KEYBOARD_CHANGE_DURATION = 200L

class TradeController(args: Bundle? = null) : ExchangeController.ChildController(args) {

    private val binding by viewBinding(ControllerExchangeTradeBinding::inflate)

    private var isPinPadVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(view: View) {
        super.onCreateView(view)

        with(binding) {
            val white = getColor(R.color.white)

            pinpad.setDeleteButtonTint(white)
            pinpad.setButtonTextColor(IntArray(11) { white })
            pinpad.setBRButtonBackgroundResId(R.drawable.keyboard_blue_pill_button, true)

            toAsset.labelAlt.isVisible = false
            fromAsset.labelInput.setOnClickListener { togglePinPad() }

            tradeTouchInterceptor.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN && isPinPadVisible) {
                    hidePinPad()
                    return@setOnTouchListener true
                }
                false
            }

            fromAsset.labelInput.doOnTextChanged { _, _, _, _ ->
                fromAsset.labelInput.updateToAssetVisibility()
            }

            toAsset.buttonAsset.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnSelectPairClicked(false))
            }
            fromAsset.buttonAsset.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnSelectPairClicked(true))
            }

            buttonClose.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnCloseClicked(false))
            }

            buttonSwapAssets.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnSwapCurrenciesClicked)
            }

            buttonPreview.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnContinueClicked)
            }

            buttonMax.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnMaxAmountClicked)
                hidePinPad()
            }

            buttonDone.setOnClickListener {
                hidePinPad()
            }

            pinpad.apply {
                setShowDecimal(true)
                setOnInsertListener { key ->
                    eventConsumer.accept(
                        when {
                            key.isEmpty() -> ExchangeEvent.OnAmountChange.Delete
                            key[0] == '.' -> ExchangeEvent.OnAmountChange.Decimal
                            Character.isDigit(key[0]) -> ExchangeEvent.OnAmountChange.Digit(key.toInt())
                            else -> return@setOnInsertListener
                        }
                    )
                }
            }
        }
    }

    override fun handleBack(): Boolean {
        return if (isPinPadVisible) {
            hidePinPad()
            true
        } else super.handleBack()
    }

    override fun ExchangeModel.render() = with(binding) {
        val res = requireResources()
        ifChanged(ExchangeModel::sourceCurrencyCode) { sourceCurrencyCode ->
            fromAsset.configureAssetView(sourceCurrencyCode)
        }

        ifChanged(
            ExchangeModel::sourceCurrencyCode,
            ExchangeModel::formattedCryptoBalances,
            ExchangeModel::inputError
        ) {
            labelAvailableBalance.text = formattedCryptoBalances[sourceCurrencyCode]
                ?.let { balance -> res.getString(R.string.Exchange_available, balance) }

            when (inputError) {
                is ExchangeModel.InputError.BalanceLow -> getColor(R.color.ui_error)
                else -> getColor(R.color.white_transparent_60)
            }.run(labelAvailableBalance::setTextColor)
        }

        ifChanged(ExchangeModel::quoteCurrencyCode) {
            toAsset.configureAssetView(quoteCurrencyCode)
        }

        ifChanged(ExchangeModel::formattedSourceAmount) {
            fromAsset.labelInput.text = formattedSourceAmount?.split(" ")?.firstOrNull()
        }

        ifChanged(ExchangeModel::formattedSourceAmountFiatValue) {
            fromAsset.labelAlt.text = formattedSourceAmountFiatValue
            fromAsset.labelAlt.isVisible = formattedSourceAmountFiatValue != null
        }

        ifChanged(ExchangeModel::formattedQuoteAmount) {
            toAsset.labelInput.text = formattedQuoteAmount
        }

        ifChanged(ExchangeModel::selectedOffer, ExchangeModel::offerState) {
            val offerState = offerState
            when (offerState) {
                ExchangeModel.OfferState.IDLE -> {
                    labelWith.isVisible = false
                    offer.root.isVisible = false
                    buttonPreview.isVisible = false
                }
                ExchangeModel.OfferState.GATHERING -> {
                    labelWith.isVisible = true
                    offer.groupOfferDetails.isVisible = false
                    offer.progress.isVisible = true
                    offer.root.isVisible = true
                    buttonPreview.isVisible = true
                    buttonPreview.isEnabled = false
                }
                ExchangeModel.OfferState.NO_OFFERS -> {
                    labelWith.isVisible = true
                    offer.groupOfferDetails.isVisible = true
                    offer.layoutOfferInfo.isVisible = false
                    offer.progress.isVisible = false
                    offer.root.isVisible = true
                    buttonPreview.isVisible = true
                    buttonPreview.isEnabled = false
                }
                ExchangeModel.OfferState.COMPLETED -> {
                    labelWith.isVisible = true
                    offer.progress.isVisible = false
                    offer.root.isVisible = true
                    buttonPreview.isVisible = true
                    hidePinPad()
                }
            }

            val selectedOffer = selectedOffer
            if (offerState == ExchangeModel.OfferState.COMPLETED) {
                when (selectedOffer) {
                    is ExchangeModel.OfferDetails.ValidOffer -> {
                        selectedOffer.setProviderIcon(offer.imageProviderLogo)
                        offer.labelProviderName.text = selectedOffer.offer.provider.name
                        offer.labelRateValue.text = selectedOffer.formattedSourceRatePerQuote
                        offer.labelFeeValue.text = selectedOffer.run { formattedQuoteFees ?: formattedSourceFees }
                        offer.groupOfferDetails.isVisible = true
                        offer.layoutOfferInfo.isVisible = true
                        buttonPreview.isEnabled = true
                        buttonPreview.setText(R.string.Exchange_CTA_preview)
                    }
                    is ExchangeModel.OfferDetails.InvalidOffer -> {
                        selectedOffer.setProviderIcon(offer.imageProviderLogo)
                        offer.labelProviderName.text = selectedOffer.offer.provider.name
                        offer.groupOfferDetails.isVisible = true
                        offer.layoutOfferInfo.isVisible = false
                        when {
                            !selectedOffer.formattedMinSourceAmount.isNullOrBlank() -> {
                                buttonPreview.isEnabled = true
                                buttonPreview.text = res.getString(
                                    R.string.Exchange_CTA_setMin,
                                    selectedOffer.formattedMinSourceAmount
                                )
                            }
                            !selectedOffer.formattedMaxSourceAmount.isNullOrBlank() -> {
                                buttonPreview.isEnabled = true
                                buttonPreview.text = res.getString(
                                    R.string.Exchange_CTA_setMax,
                                    selectedOffer.formattedMaxSourceAmount
                                )
                            }
                            else -> {
                                buttonPreview.isEnabled = false
                                buttonPreview.setText(R.string.Exchange_CTA_preview)
                            }
                        }
                    }
                    null -> {
                        offer.groupOfferDetails.isVisible = true
                        offer.layoutOfferInfo.isVisible = true
                        offer.imageProviderLogo.setImageDrawable(null)
                        offer.labelProviderName.text = null
                        offer.labelRateValue.text = null
                        offer.labelFeeValue.text = null
                        buttonPreview.setText(R.string.Exchange_CTA_preview)
                    }
                }
            }
        }
    }

    private fun LayoutTradeAssetBinding.configureAssetView(currencyCode: String?) {
        labelCurrencyCode.text = currencyCode?.toUpperCase(Locale.ROOT)
        val startColor = currencyCode?.run(TokenUtil::getTokenStartColor)?.run(Color::parseColor)
        val endColor = currencyCode?.run(TokenUtil::getTokenEndColor)?.run(Color::parseColor)

        val drawable = (layoutBackground.background.mutate() as GradientDrawable)
        if (startColor == null) {
            drawable.setColor(getColor(R.color.dark_gray))
        } else {
            drawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT
            drawable.colors = if (endColor != null) {
                intArrayOf(startColor, endColor)
            } else {
                intArrayOf(startColor)
            }
        }
        layoutBackground.background = drawable

        if (currencyCode.isNullOrBlank()) {
            cardCurrencyIcon.isVisible = false
            imageCurrencyIcon.setImageDrawable(null)
        } else {
            val currencyIcon = TokenUtil.getTokenIconPath(currencyCode, false)
            if (currencyIcon.isNullOrBlank()) {
                imageCurrencyIcon.setImageDrawable(null)
            } else {
                Picasso.get()
                    .load(File(currencyIcon))
                    .into(imageCurrencyIcon)
            }
            cardCurrencyIcon.isVisible = true
        }
    }

    private fun togglePinPad() {
        if (isPinPadVisible) {
            hidePinPad()
        } else {
            showPinPad()
        }
        updateActiveAsset()
    }

    private fun showPinPad() {
        Slide(Gravity.BOTTOM).apply {
            duration = KEYBOARD_CHANGE_DURATION
            addTarget(binding.pinpadContainer)
            TransitionManager.beginDelayedTransition(binding.root, this)
        }

        binding.pinpadContainer.isVisible = true
        isPinPadVisible = true
    }

    private fun hidePinPad() {
        Slide(Gravity.BOTTOM).apply {
            duration = KEYBOARD_CHANGE_DURATION
            addTarget(binding.pinpadContainer)
            TransitionManager.beginDelayedTransition(binding.root, this)
        }

        binding.pinpadContainer.isVisible = false
        isPinPadVisible = false
        updateActiveAsset()
    }

    private fun updateActiveAsset() {
        val res = requireResources()
        val activeBorderColor = getColor(R.color.primary_action_button)
        val inactiveColor = getColor(R.color.black)
        val (fromColor, toColor) = if (isPinPadVisible) {
            activeBorderColor to 0
        } else {
            0 to 0
        }
        binding.fromAsset.card.strokeColor = fromColor
        binding.toAsset.card.strokeColor = toColor

        val background = ResourcesCompat.getDrawable(
            res,
            R.drawable.crypto_card_shape,
            view?.context?.theme
        )

        // hides zero value for the From Asset label when pin pad is visible
        binding.fromAsset.labelInput.updateToAssetVisibility()

        val foreground = checkNotNull(background).mutate() as GradientDrawable
        foreground.setColor(inactiveColor)
        foreground.alpha = 125
        when {
            fromColor != 0 -> {
                binding.toAsset.layoutBackground.foreground = foreground
                binding.fromAsset.layoutBackground.foreground = null
                binding.fromAsset.card.strokeWidth = res.getDimensionPixelSize(R.dimen.active_token_card_stroke_width)
                binding.toAsset.card.strokeWidth = 0
            }
            toColor != 0 -> {
                binding.fromAsset.layoutBackground.foreground = foreground
                binding.toAsset.layoutBackground.foreground = null
                binding.toAsset.card.strokeWidth = res.getDimensionPixelSize(R.dimen.active_token_card_stroke_width)
                binding.fromAsset.card.strokeWidth = 0
            }
            else -> {
                binding.fromAsset.layoutBackground.foreground = null
                binding.fromAsset.card.strokeWidth = 0
                binding.toAsset.layoutBackground.foreground = null
                binding.toAsset.card.strokeWidth = 0
            }
        }
    }

    /** hides zero value for the From Asset label when pin pad is visible */
    private fun TextView.updateToAssetVisibility() {
        isInvisible = text.toString() == "0" && isPinPadVisible
    }
}
