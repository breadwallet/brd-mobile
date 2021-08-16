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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.databinding.ControllerExchangeSettingsBinding

class ExchangeSettingsController(args: Bundle? = null) : ExchangeController.ChildController(args) {

    private val binding by viewBinding(ControllerExchangeSettingsBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)

        with(binding) {
            root.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            cellCountry.labelTitle.setText(R.string.Exchange_Settings_country)
            cellRegion.labelTitle.setText(R.string.Exchange_Settings_region)
            cellCurrency.labelTitle.setText(R.string.Exchange_Settings_currency)
            cellCountry.root.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnConfigureCountryClicked)
            }
            cellRegion.root.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnConfigureRegionClicked)
            }
            cellCurrency.root.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnConfigureCurrencyClicked)
            }
            buttonNext.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnContinueClicked)
            }
            buttonClose.setOnClickListener {
                eventConsumer.accept(ExchangeEvent.OnCloseClicked(confirmed = false))
            }
        }
    }

    override fun ExchangeModel.render() = with(binding) {
        ifChanged(ExchangeModel::selectedFiatCurrency) { selectedFiatCurrency ->
            cellCurrency.labelValue.text = selectedFiatCurrency?.selectedFiatCurrencyName()
            cellCurrency.labelValue.isGone = selectedFiatCurrency == null
        }

        ifChanged(ExchangeModel::selectedCountry) { selectedCountry ->
            cellCountry.labelValue.text = selectedCountry?.name
            cellCountry.labelValue.isGone = selectedCountry == null

            cellRegion.root.isVisible = selectedCountry?.regions.orEmpty().isNotEmpty()
        }

        ifChanged(ExchangeModel::selectedRegion) { selectedRegion ->
            cellRegion.labelValue.text = selectedRegion?.name
        }

        ifChanged(ExchangeModel::settingsOnly) { settingsOnly ->
            buttonNext.isVisible = !settingsOnly
        }
    }
}
