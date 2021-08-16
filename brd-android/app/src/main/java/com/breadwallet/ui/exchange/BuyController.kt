/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.brd.exchange.ExchangeEffect
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.databinding.ControllerExchangeBuyBinding
import com.breadwallet.tools.animation.SpringAnimator
import com.breadwallet.tools.util.TokenUtil
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

private const val LOADING_ALPHA = .25f

class BuyController(args: Bundle? = null) : ExchangeController.ChildController(args) {

    private val binding by viewBinding(ControllerExchangeBuyBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        with(binding) {
            val white = getColor(R.color.white)

            layoutPinPad.setDeleteButtonTint(white)
            layoutPinPad.setButtonTextColor(IntArray(11) { white })
            cellSelectedOffer.apply {
                val titleColors = labelTitle.textColors
                val valueColors = labelValue.textColors
                labelTitle.setTextColor(valueColors)
                labelValue.setTextColor(titleColors)
                labelValue.isVisible = true
                labelTitle.isVisible = true
                iconColor.isVisible = true
            }

            cellQuoteCurrency.apply {
                labelTitle.isVisible = false
                icon.isVisible = true
                iconLetter.isVisible = true
                iconColor.isVisible = true
                iconColor.imageTintList = null
                labelTitle.setTextAppearance(R.style.textAppearanceBody1)
            }
            buttonClose.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnCloseClicked(confirmed = false))
            }
            cellSelectedOffer.root.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnSelectOfferClicked(cancel = false))
            }
            cellQuoteCurrency.root.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnSelectPairClicked(selectSource = false))
            }

            layoutPinPad.apply {
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
            buttonContinue.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnContinueClicked)
            }
        }
    }

    override fun ExchangeModel.render() = with(binding) {
        ifChanged(ExchangeModel::test) {
            labelTest.isVisible = test
        }

        ifChanged(ExchangeModel::inputError) {
            when (inputError) {
                null -> {
                    labelError.text = null
                }
                is ExchangeModel.InputError.BalanceLow -> {
                    labelError.text = "Balance too low!"
                }
            }
        }

        ifChanged(ExchangeModel::quoteCurrencyCode) { quoteCurrencyCode ->
            if (quoteCurrencyCode != null) {
                val quoteIcon = TokenUtil.getTokenIconPath(quoteCurrencyCode, false)
                cellQuoteCurrency.iconLetter.text = if (quoteIcon.isNullOrBlank()) {
                    cellQuoteCurrency.icon.setImageDrawable(null)
                    quoteCurrencyCode.first().toString().toUpperCase(Locale.ROOT)
                } else {
                    Picasso.get().load(File(quoteIcon)).into(cellQuoteCurrency.icon)
                    null
                }

                val color = TokenUtil.getTokenStartColor(quoteCurrencyCode)
                    ?.run(Color::parseColor)
                    ?: Color.TRANSPARENT
                cellQuoteCurrency.iconColor.imageTintList = ColorStateList.valueOf(color)
                cellQuoteCurrency.iconColor.imageTintMode = PorterDuff.Mode.SRC
            }

            cellQuoteCurrency.labelValue.text = currencies[quoteCurrencyCode]?.name
        }

        ifChanged(ExchangeModel::sourceAmountInput, ExchangeModel::formattedSourceAmount) {
            labelSourceAmount.text = formattedSourceAmount
            labelSourceAmount.setTextColor(
                if (sourceAmountInput == "0") {
                    getColor(R.color.light_gray)
                } else {
                    getColor(R.color.white)
                }
            )
        }

        ifChanged(ExchangeModel::formattedQuoteAmount) {
            labelQuoteAmount.text = formattedQuoteAmount
        }

        ifChanged(ExchangeModel::offerState) {
            when (offerState) {
                ExchangeModel.OfferState.IDLE -> {
                    buttonContinue.setText(R.string.Exchange_CTA_next)
                    buttonContinue.isEnabled = false
                    cellSelectedOffer.apply {
                        imageBackIcon.isVisible = false
                        icon.setImageResource(R.drawable.ic_shop)
                        progress.isVisible = false
                        labelTitle.setText(R.string.Exchange_offer_initTitle)
                        labelValue.isVisible = false
                        labelValue.setTextColor(getColor(R.color.light_gray))
                    }
                }
                ExchangeModel.OfferState.COMPLETED -> {
                    cellSelectedOffer.apply {
                        imageBackIcon.isVisible = offerDetails.size > 1
                        progress.isVisible = false
                    }
                }
                ExchangeModel.OfferState.GATHERING -> {
                    buttonContinue.setText(R.string.Exchange_CTA_next)
                    buttonContinue.isEnabled = false
                    cellSelectedOffer.apply {
                        imageBackIcon.isVisible = false
                        icon.setImageDrawable(null)
                        labelTitle.setText(R.string.Exchange_offer_gatheringTitle)
                        labelValue.setText(R.string.Exchange_offer_gatheringSubtitle)
                        labelValue.isVisible = true
                        progress.isVisible = true
                        labelValue.setTextColor(getColor(R.color.light_gray))
                    }
                }
                ExchangeModel.OfferState.NO_OFFERS -> {
                    buttonContinue.setText(R.string.Exchange_CTA_next)
                    buttonContinue.isEnabled = false
                    cellSelectedOffer.apply {
                        imageBackIcon.isVisible = false
                        icon.setImageResource(R.drawable.ic_no_offers)
                        labelTitle.setText(R.string.Exchange_offer_noneTitle)
                        labelValue.setText(R.string.Exchange_offer_noneSubtitle)
                        labelValue.isVisible = true
                        progress.isVisible = false
                        labelValue.setTextColor(getColor(R.color.light_gray))
                    }
                }
            }
        }

        ifChanged(ExchangeModel::selectedOffer) {
            onSelectedOfferChanged()
        }
        ifChanged(ExchangeModel::state) {
            when (state) {
                is ExchangeModel.State.OrderSetup -> {
                    cellQuoteCurrency.progress.isVisible = false
                    cellSelectedOffer.root.animate().alpha(1f).start()
                    labelSourceAmount.animate().alpha(1f).start()
                    layoutPinPad.animate().alpha(1f).start()
                    buttonContinue.animate().alpha(1f).start()
                }
                is ExchangeModel.State.Initializing -> {
                    cellQuoteCurrency.progress.isVisible = true
                    cellQuoteCurrency.labelValue.setText(R.string.Exchange_loadingAssets)
                    cellSelectedOffer.root.alpha = LOADING_ALPHA
                    labelSourceAmount.alpha = LOADING_ALPHA
                    layoutPinPad.alpha = LOADING_ALPHA
                    buttonContinue.alpha = LOADING_ALPHA
                }
                else -> Unit
            }
        }
    }

    override fun handleEffect(effect: ExchangeEffect) {
        if (effect is ExchangeEffect.ErrorSignal) {
            SpringAnimator.failShakeAnimation(applicationContext, binding.labelSourceAmount)
        }
    }

    private fun ExchangeModel.onSelectedOfferChanged(): Unit = with(binding) {
        val res = requireResources()
        cellSelectedOffer.icon.isVisible = true
        cellSelectedOffer.iconColor.isVisible = true

        when (val selectedOffer = selectedOffer) {
            null -> {
                cellSelectedOffer.labelValue.setTextColor(getColor(R.color.light_gray))
                buttonContinue.setText(R.string.Exchange_CTA_next)
                buttonContinue.isEnabled = false
            }
            is ExchangeModel.OfferDetails.ValidOffer -> {
                buttonContinue.setText(R.string.Exchange_CTA_next)
                buttonContinue.isEnabled = true

                cellSelectedOffer.apply {
                    labelValue.setTextColor(getColor(R.color.light_gray))
                    labelTitle.text =
                        "${selectedOffer.offer.provider.name} by ${selectedOffer.offer.sourceCurrencyMethod::class.simpleName}"
                    labelValue.text =
                        "Rate: ${selectedOffer.formattedSourceRate} - Total: ${selectedOffer.formattedSourceTotal}"
                    labelTitle.isVisible = true
                }

                selectedOffer.setProviderIcon(cellSelectedOffer.icon)
            }
            is ExchangeModel.OfferDetails.InvalidOffer -> {
                cellSelectedOffer.apply {
                    labelTitle.text =
                        "${selectedOffer.offer.provider.name} by ${selectedOffer.offer.sourceCurrencyMethod::class.simpleName}"
                    labelTitle.isVisible = true
                }

                when {
                    !selectedOffer.formattedMinSourceAmount.isNullOrBlank() -> {
                        buttonContinue.isEnabled = true
                        buttonContinue.text = res.getText(R.string.Exchange_CTA_setMin, selectedOffer.formattedMinSourceAmount)
                        cellSelectedOffer.labelValue.text = "Minimum amount ${selectedOffer.formattedMinSourceAmount}"
                        cellSelectedOffer.labelValue.setTextColor(getColor(R.color.ui_error))
                    }
                    !selectedOffer.formattedMaxSourceAmount.isNullOrBlank() -> {
                        buttonContinue.isEnabled = true
                        buttonContinue.text = res.getText(R.string.Exchange_CTA_setMax, selectedOffer.formattedMaxSourceAmount)
                        cellSelectedOffer.labelValue.text = "Maximum amount ${selectedOffer.formattedMaxSourceAmount}"
                        cellSelectedOffer.labelValue.setTextColor(getColor(R.color.ui_error))
                    }
                    else -> {
                        buttonContinue.isEnabled = false
                        buttonContinue.setText(R.string.Exchange_CTA_next)
                    }
                }

                selectedOffer.setProviderIcon(cellSelectedOffer.icon)
            }
        }
    }
}
