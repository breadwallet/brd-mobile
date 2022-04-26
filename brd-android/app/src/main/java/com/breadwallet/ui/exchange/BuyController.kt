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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.brd.exchange.ExchangeEffect
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.databinding.ControllerExchangeBuyBinding
import com.breadwallet.tools.animation.SpringAnimator
import com.breadwallet.tools.util.TokenUtil
import com.breadwallet.util.methodStringRes
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
            layoutPinPad.setBRButtonBackgroundResId(R.drawable.keyboard_blue_pill_button, true)
            cellSelectedOffer.apply {
                val titleColors = labelTitle.textColors
                val valueColors = labelValue.textColors
                labelTitle.setTextColor(valueColors)
                labelValue.setTextColor(titleColors)
                labelValue.isVisible = true
                labelTitle.isVisible = true
                iconColor.isVisible = true
                iconColor.imageTintList =
                    ColorStateList.valueOf(getColor(R.color.hydra_tertiary_background))
                iconColor.imageTintMode = PorterDuff.Mode.SRC
                root.setOnClickListener {
                    eventConsumer.accept(ExchangeEvent.OnSelectOfferClicked(cancel = false))
                }
            }
            cellQuoteCurrency.apply {
                labelTitle.isVisible = false
                icon.isVisible = true
                iconLetter.isVisible = true
                iconColor.isVisible = true
                iconColor.imageTintList = null
                // TODO - apply correct textAppearance
                // labelTitle.setTextAppearance(R.style.textAppearanceBody1)
                root.setOnClickListener {
                    eventConsumer.accept(
                        ExchangeEvent.OnSelectPairClicked(selectSource = buttonSell.isSelected)
                    )
                }
            }
            buttonClose.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnCloseClicked(confirmed = false))
            }
            buySellToggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                if (!buttonBuy.isPressed && !buttonSell.isPressed) return@addOnButtonCheckedListener
                when (checkedId) {
                    R.id.buttonBuy -> ExchangeEvent.OnChangeModeClicked(mode = ExchangeModel.Mode.BUY)
                    R.id.buttonSell -> ExchangeEvent.OnChangeModeClicked(mode = ExchangeModel.Mode.SELL)
                    else -> error("Unhandled button $checkedId")
                }.run(eventConsumer::accept)
            }

            firstPreselect.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnSelectInputPresets(0))
            }

            secondPreselect.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnSelectInputPresets(1))
            }

            thirdPreselect.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnSelectInputPresets(2))
            }

            fourthPreselect.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnSelectInputPresets(3))
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

        ifChanged(ExchangeModel::inputError) { inputError ->
            labelError.isVisible = inputError is ExchangeModel.InputError.BalanceLow
            labelQuoteAmount.isVisible = inputError !is ExchangeModel.InputError.BalanceLow
            when (inputError) {
                null -> {
                    labelError.text = null
                    labelQuoteAmount.setTextColor(getColor(R.color.white_transparent_75))
                    cellQuoteCurrency.labelValue.setTextColor(getColor(R.color.white))
                }
                is ExchangeModel.InputError.InsufficientNativeCurrencyBalance -> {
                    cellSelectedOffer.labelValue.text = resources?.getString(
                        R.string.Exchange_ErrorState_insufficientNativeBalanceError,
                        inputError.currencyCode
                    )
                    cellSelectedOffer.labelTitle.text = resources?.getString(
                        R.string.Exchange_ErrorState_insufficientNativeBalanceErrorSecondary,
                        inputError.fee,
                        inputError.currencyCode
                    )
                    labelQuoteAmount.setTextColor(getColor(R.color.brdRed))
                }
                is ExchangeModel.InputError.BalanceLow -> {
                    labelError.setText(R.string.Exchange_InputError_BalanceLow)
                    if (mode.isSell) {
                        cellQuoteCurrency.labelValue.setTextColor(getColor(R.color.brdRed))
                    }
                }
            }
        }

        ifChanged(
            ExchangeModel::quoteCurrencyCode,
            ExchangeModel::sourceCurrencyCode,
            ExchangeModel::formattedCryptoBalances
        ) {
            val quoteCurrencyCode = quoteCurrencyCode
            val sourceCurrencyCode = sourceCurrencyCode
            if (quoteCurrencyCode != null && mode == ExchangeModel.Mode.BUY) {
                setCellQuoteCurrency(quoteCurrencyCode, this@render)
            } else if (sourceCurrencyCode != null && mode == ExchangeModel.Mode.SELL) {
                setCellQuoteCurrency(sourceCurrencyCode, this@render)
                cellQuoteCurrency.labelValue.text = resources?.getString(
                    R.string.Exchange_available,
                    formattedCryptoBalances[sourceCurrencyCode.orEmpty()]
                )
            } else {
                cellQuoteCurrency.labelTitle.isVisible = false
                cellQuoteCurrency.labelValue.text = currencies[quoteCurrencyCode]?.name
            }
        }

        ifChanged(ExchangeModel::sourceAmountInput, ExchangeModel::formattedSourceAmount) {
            labelSourceAmount.text = when {
                mode.isSell && sourceAmountInput.isEmpty() -> "0"
                mode.isSell -> sourceAmountInput
                else -> formattedSourceAmount
            }
            labelSourceAmount.setTextColor(
                getColor(if (sourceAmountInput == "0") R.color.light_gray else R.color.white)
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
            val state = state
            inputGroup.isVisible = true
            errorGroup.isVisible = false
            when (state) {
                is ExchangeModel.State.EmptyWallets -> {
                    if (mode.isSell) {
                        inputGroup.isVisible = false
                        errorGroup.isVisible = true
                        when {
                            state.sellingUnavailable -> {
                                errorTitle.setText(R.string.Exchange_FullScreenErrorState_sellingUnavailableTitle)
                                errorBody.text = resources?.getString(
                                    R.string.Exchange_FullScreenErrorState_sellingUnavailableBody,
                                    selectedFiatCurrency?.code?.toUpperCase(),
                                    selectedCountry?.name
                                )
                                errorButtonContinue.setText(R.string.Exchange_FullScreenErrorState_sellingUnavailableCTA)
                                val drawable = requireContext().getLayerDrawable(
                                    colorRes = R.color.hydra_secondary_background,
                                    drawableRes = R.drawable.ic_rain
                                )
                                errorIcon.setImageDrawable(drawable)
                                errorButtonContinue.setOnClickListener {
                                    eventConsumer.accept(ExchangeEvent.OnConfigureSettingsClicked)
                                }
                            }
                            state.invalidSellPairs -> {
                                errorTitle.setText(R.string.Exchange_FullScreenErrorState_sellingNoAssetsTitle)
                                errorBody.setText(R.string.Exchange_FullScreenErrorState_sellingNoAssetsBody)
                                errorButtonContinue.setText(R.string.Exchange_FullScreenErrorState_sellingNoAssetsCTA)
                                val drawable = requireContext().getLayerDrawable(
                                    colorRes = R.color.hydra_secondary_background,
                                    drawableRes = R.drawable.ic_money_wings
                                )
                                errorIcon.setImageDrawable(drawable)
                                errorButtonContinue.setOnClickListener {
                                    eventConsumer.accept(
                                        ExchangeEvent.OnChangeModeClicked(ExchangeModel.Mode.BUY)
                                    )
                                }
                            }
                            else -> {
                                errorTitle.setText(R.string.Exchange_FullScreenErrorState_emptyWalletTitle)
                                errorBody.setText(R.string.Exchange_FullScreenErrorState_emptyWalletBody)
                                errorButtonContinue.setText(R.string.Exchange_FullScreenErrorState_emptyWalletCTA)
                                val drawable = requireContext().getLayerDrawable(
                                    colorRes = R.color.hydra_secondary_background,
                                    drawableRes = R.drawable.ic_cry
                                )
                                errorIcon.setImageDrawable(drawable)
                                errorButtonContinue.setOnClickListener {
                                    eventConsumer.accept(
                                        ExchangeEvent.OnChangeModeClicked(ExchangeModel.Mode.BUY)
                                    )
                                }
                            }
                        }
                    }
                }
                is ExchangeModel.State.OrderSetup -> {
                    cellQuoteCurrency.progress.isVisible = false
                    cellSelectedOffer.root.animate().alpha(1f).start()
                    labelSourceAmount.animate().alpha(1f).start()
                    layoutPinPad.animate().alpha(1f).start()
                    buttonContinue.animate().alpha(1f).start()
                }
                is ExchangeModel.State.Initializing -> {
                    inputGroup.isVisible = true
                    errorGroup.isVisible = false
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
        ifChanged(ExchangeModel::inputPresets) {
            preSelectToggleGroup.isGone = inputPresets.isEmpty()
            firstPreselect.text = inputPresets.getOrNull(0)?.formattedAmount.orEmpty()
            secondPreselect.text = inputPresets.getOrNull(1)?.formattedAmount.orEmpty()
            thirdPreselect.text = inputPresets.getOrNull(2)?.formattedAmount.orEmpty()
            fourthPreselect.text = inputPresets.getOrNull(3)?.formattedAmount.orEmpty()
        }
        ifChanged(ExchangeModel::selectedInputPreset) {
            firstPreselect.isSelected = selectedInputPreset == 0
            secondPreselect.isSelected = selectedInputPreset == 1
            thirdPreselect.isSelected = selectedInputPreset == 2
            fourthPreselect.isSelected = selectedInputPreset == 3
        }
        ifChanged(ExchangeModel::mode) { mode ->
            buttonBuy.isSelected = mode.isBuy
            buttonSell.isSelected = !mode.isBuy
            val titleColor = if (mode.isBuy) R.color.light_gray else R.color.white
            val valueColor = if (mode.isBuy) R.color.white else R.color.light_gray
            cellQuoteCurrency.labelTitle.setTextColor(getColor(titleColor))
            cellQuoteCurrency.labelValue.setTextColor(getColor(valueColor))
        }
    }

    private fun setCellQuoteCurrency(currencyCode: String, model: ExchangeModel) = with(binding) {
        val quoteIcon = TokenUtil.getTokenIconPath(currencyCode, false)
        binding.cellQuoteCurrency.iconLetter.text = if (quoteIcon.isNullOrBlank()) {
            binding.cellQuoteCurrency.icon.setImageDrawable(null)
            currencyCode.first().toString().toUpperCase(Locale.ROOT)
        } else {
            Picasso.get().load(File(quoteIcon)).into(binding.cellQuoteCurrency.icon)
            null
        }

        val color = TokenUtil.getTokenStartColor(currencyCode)
            ?.run(Color::parseColor)
            ?: Color.TRANSPARENT
        binding.cellQuoteCurrency.iconColor.imageTintList = ColorStateList.valueOf(color)
        binding.cellQuoteCurrency.iconColor.imageTintMode = PorterDuff.Mode.SRC
        binding.cellQuoteCurrency.labelTitle.isVisible = model.mode.isSell
        if (model.mode.isSell) {
            binding.cellQuoteCurrency.labelTitle.text = model.currencies[currencyCode]?.name
        } else {
            binding.cellQuoteCurrency.labelValue.text = model.currencies[currencyCode]?.name
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
                    val methodStringRes = selectedOffer.offer.sourceCurrencyMethod.methodStringRes
                    labelTitle.text =
                        res.getString(methodStringRes, selectedOffer.offer.provider.name)
                    labelValue.text = buildString {
                        append(res.getString(R.string.Exchange_offer_rate))
                        append(" ${selectedOffer.formattedSourceRate}")
                        append(" - ")
                        append(res.getString(R.string.Exchange_offer_total))
                        append(" ${selectedOffer.formattedSourceTotal}")
                    }
                    labelTitle.isVisible = true
                }

                selectedOffer.setProviderIcon(cellSelectedOffer.icon)
            }
            is ExchangeModel.OfferDetails.InvalidOffer -> {
                cellSelectedOffer.apply {
                    val methodStringRes = selectedOffer.offer.sourceCurrencyMethod.methodStringRes
                    labelTitle.text =
                        res.getString(methodStringRes, selectedOffer.offer.provider.name)
                    labelTitle.isVisible = true
                }

                when {
                    !selectedOffer.formattedMinSourceAmount.isNullOrBlank() -> {
                        buttonContinue.isEnabled = true
                        buttonContinue.text = res.getString(
                            R.string.Exchange_CTA_setMin,
                            selectedOffer.formattedMinSourceAmount
                        )
                        cellSelectedOffer.labelValue.text = buildString {
                            append(res.getString(R.string.Exchange_offer_minAmount))
                            append(" ${selectedOffer.formattedMinSourceAmount}")
                        }
                        cellSelectedOffer.labelValue.setTextColor(getColor(R.color.ui_error))
                    }
                    !selectedOffer.formattedMaxSourceAmount.isNullOrBlank() -> {
                        buttonContinue.isEnabled = true
                        buttonContinue.text = res.getString(
                            R.string.Exchange_CTA_setMax,
                            selectedOffer.formattedMaxSourceAmount
                        )
                        cellSelectedOffer.labelValue.text = buildString {
                            append(res.getString(R.string.Exchange_offer_maxAmount))
                            append(" ${selectedOffer.formattedMaxSourceAmount}")
                        }
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
