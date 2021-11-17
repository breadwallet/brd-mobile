/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.exchange

import com.brd.api.models.ExchangeOffer
import com.brd.api.models.ExchangeOfferBody
import com.brd.api.models.ExchangeOrder
import com.brd.api.models.ExchangePair

sealed class ExchangeEffect {

    object LoadFeaturePromotions : ExchangeEffect()

    data class UpdateFeaturePromotionShown(val mode: ExchangeModel.Mode) : ExchangeEffect()

    object LoadUserPreferences : ExchangeEffect()

    object LoadCountries : ExchangeEffect()

    data class LoadWalletBalances(
        val fiatCurrencyCode: String,
        val pairs: List<ExchangePair>,
    ) : ExchangeEffect() {
        override fun toString(): String {
            return "LoadWalletBalances(" +
                    "fiatCurrencyCode='$fiatCurrencyCode', " +
                    "pairs=(size:${pairs.size})" +
                    ")"
        }
    }

    data class LoadPairs(
        val countryCode: String,
        val regionCode: String?,
        val selectedFiatCurrencyCode: String?,
        val test: Boolean = false,
    ) : ExchangeEffect()

    data class RequestOffers(
        val body: ExchangeOfferBody?,
        val mode: ExchangeModel.Mode,
    ) : ExchangeEffect()

    data class CreateOrder(
        val offer: ExchangeOffer
    ) : ExchangeEffect()

    data class UpdateRegionPreferences(
        val countryCode: String,
        val regionCode: String?,
    ) : ExchangeEffect()

    data class UpdateCurrencyPreference(
        val currencyCode: String,
    ) : ExchangeEffect()

    data class TrackEvent(
        val name: String,
        val props: Map<String, String> = emptyMap(),
    ) : ExchangeEffect()

    data class ProcessBackgroundActions(
        val order: ExchangeOrder,
    ) : ExchangeEffect()

    data class ProcessUserAction(
        val order: ExchangeOrder,
        val baseUrl: String,
        val action: ExchangeOrder.Action,
    ) : ExchangeEffect()

    data class SubmitCryptoTransferHash(
        val order: ExchangeOrder,
        val action: ExchangeOrder.Action,
        val transactionHash: String,
    ) : ExchangeEffect()

    data class UpdateLastOrderCurrency(
        val currencyCode: String,
    ) : ExchangeEffect()

    data class UpdateLastSellCurrency(
        val currencyCode: String,
    ) : ExchangeEffect()

    data class UpdateLastTradeCurrencyPair(
        val sourceCode: String?,
        val quoteCode: String?,
    ) : ExchangeEffect()

    data class UpdateLastOrderAmount(
        val amount: String,
    ) : ExchangeEffect()

    data class LoadNativeNetworkInfo(
        val currencyId: String,
    ) : ExchangeEffect()

    object ExitFlow : ExchangeEffect()

    object ErrorSignal : ExchangeEffect()
}
