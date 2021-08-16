/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.exchange

import com.brd.api.BrdApiHost
import com.brd.api.models.*


sealed class ExchangeEvent {

    /**
     * Feedback effect for [ExchangeEffect.LoadCountries] containing
     * supported [countries] and the server's guess of user location
     * in [defaultCountryCode] and [defaultRegionCode].
     */
    data class OnCountriesLoaded(
        val countries: List<ExchangeCountry>,
        val defaultCountryCode: String?,
        val defaultRegionCode: String?,
    ) : ExchangeEvent() {
        override fun toString(): String {
            return "OnCountriesChanged(" +
                    "countries=${countries.size}, " +
                    "defaultCountryCode=$defaultCountryCode, " +
                    "defaultRegionCode=$defaultRegionCode)"
        }
    }
    /**
     * Error feedback effect for [ExchangeEffect.LoadCountries].
     */
    data class OnCountriesError(
        val error: ExchangeCountriesResult.Error,
    ) : ExchangeEvent()

    /**
     * Feedback effect for [ExchangeEffect.LoadUserPreferences].
     */
    data class OnUserPreferencesLoaded(
        val selectedCountryCode: String?,
        val selectedRegionCode: String?,
        val fiatCurrencyCode: String?,
        val lastPurchaseCurrencyCode: String?,
        val lastTradeSourceCurrencyCode: String?,
        val lastTradeQuoteCurrencyCode: String?,
        val lastOrderAmount: String?,
        val apiHost: BrdApiHost,
    ) : ExchangeEvent()

    /**
     * Feedback effect for [ExchangeEffect.LoadPairs].
     */
    data class OnPairsLoaded(
        val pairs: List<ExchangePair>,
        val currencies: Map<String, ExchangeCurrency>,
        val formattedFiatRates: Map<String, String>,
    ) : ExchangeEvent() {
        override fun toString(): String {
            return "OnPairsLoaded(" +
                    "pairs=${pairs.size}, " +
                    "currencies=${currencies.size}, " +
                    "formattedFiatRates=${formattedFiatRates.size})"
        }
    }

    /**
     * Error feedback effect for [ExchangeEffect.LoadPairs].
     */
    data class OnPairsError(
        val error: ExchangePairsResult.Error,
    ) : ExchangeEvent()

    data class OnOfferAmountOverridden(
        val originalBody: ExchangeOfferBody,
        val newAmount: Double
    ) : ExchangeEvent()

    /**
     * Feedback effect for [ExchangeEffect.RequestOffers].
     */
    data class OnOfferRequestUpdated(
        val offerBody: ExchangeOfferBody,
        val exchangeOfferRequest: ExchangeOfferRequest,
        val offerDetails: List<ExchangeModel.OfferDetails>,
    ) : ExchangeEvent()

    /**
     * Error feedback effect for [ExchangeEffect.RequestOffers].
     */
    data class OnOfferRequestError(
        val offerBody: ExchangeOfferBody,
        val error: ExchangeOfferRequestResult.Error,
    ) : ExchangeEvent()

    /**
     * User event containing an [ExchangeOffer] from [ExchangeModel.offerDetails].
     *
     * When [offerDetails] is an instance of [ExchangeModel.OfferDetails.InvalidOffer]
     * and [adjustToLimit] is true, the target partner limit will be set
     * automatically with the provider limit.
     *
     * @see [ExchangeModel.State.OrderSetup.selectingOffer]
     * @see [ExchangeEvent.OnSelectOfferClicked]
     */
    data class OnOfferClicked(
        val offerDetails: ExchangeModel.OfferDetails,
        val adjustToLimit: Boolean,
    ) : ExchangeEvent()

    /**
     * User events to update [ExchangeModel.sourceAmountInput].
     */
    sealed class OnAmountChange : ExchangeEvent() {
        /**
         * Append decimal.
         */
        object Decimal : OnAmountChange()

        /**
         * Remove the last character.
         */
        object Delete : OnAmountChange()

        /**
         * Remove all characters.
         */
        object Clear : OnAmountChange()

        /**
         * Append [digit].
         */
        data class Digit(val digit: Int) : OnAmountChange()
    }

    data class OnQuoteAmountChange(
        val amountChange: OnAmountChange
    ) : ExchangeEvent()

    /**
     * User event to enter [ExchangeModel.State.ConfigureSettings] with
     * [ExchangeModel.ConfigTarget.MENU].
     */
    object OnConfigureSettingsClicked : ExchangeEvent()

    /**
     * User event to enter [ExchangeModel.State.ConfigureSettings] with
     * [ExchangeModel.ConfigTarget.COUNTRY].
     */
    object OnConfigureCountryClicked : ExchangeEvent()

    /**
     * User event to enter [ExchangeModel.State.ConfigureSettings] with
     * [ExchangeModel.ConfigTarget.REGION].
     */
    object OnConfigureRegionClicked : ExchangeEvent()

    /**
     * User event to enter [ExchangeModel.State.ConfigureSettings] with
     * [ExchangeModel.ConfigTarget.CURRENCY].
     */
    object OnConfigureCurrencyClicked : ExchangeEvent()

    /**
     * User event to dismiss confirmation dialogs or configuration modals.
     */
    object OnBackClicked : ExchangeEvent()

    object OnDialogConfirmClicked : ExchangeEvent()
    object OnDialogCancelClicked : ExchangeEvent()

    /**
     * User event to dismiss the exchange flow.  When [ExchangeModel.confirmingClose]
     * is true and a confirmation modal is displayed, the positive modal button
     * should set [confirmed] to true.
     */
    data class OnCloseClicked(
        val confirmed: Boolean
    ) : ExchangeEvent()

    /**
     * User event to set [ExchangeModel.selectedCountry] from [ExchangeModel.countries].
     */
    data class OnCountryClicked(val country: ExchangeCountry) : ExchangeEvent()

    /**
     * User event to set [ExchangeModel.selectedRegion] from [ExchangeModel.selectedCountry].
     */
    data class OnRegionClicked(val region: ExchangeRegion) : ExchangeEvent()

    /**
     * User event to set [ExchangeModel.selectedFiatCurrency] from [ExchangeModel.currencies].
     */
    data class OnCurrencyClicked(val currency: ExchangeCurrency) : ExchangeEvent()

    /**
     * User event to dismiss [ExchangeModel.State.ConfigureSettings] and return to
     * [ExchangeModel.State.OrderSetup].
     */
    object OnCloseSettingsClicked : ExchangeEvent()

    /**
     * User event to proceed through the various order setup and processing states.
     */
    object OnContinueClicked : ExchangeEvent()

    /**
     * User event to set [ExchangeModel.selectedPair] from [ExchangeModel.sourcePairs].
     */
    data class OnSelectPairClicked(val selectSource: Boolean) : ExchangeEvent()

    /**
     * User event to dismiss [ExchangeModel.State.SelectAsset] and return to
     * [ExchangeModel.State.OrderSetup].
     */
    object OnSelectPairCancelClicked : ExchangeEvent()

    /**
     * User event to set [ExchangeModel.State.OrderSetup.selectingOffer].
     */
    data class OnSelectOfferClicked(
        val cancel: Boolean
    ) : ExchangeEvent()

    /**
     * User event to swap [ExchangeModel.sourceCurrencyCode] and [ExchangeModel.quoteCurrencyCode].
     */
    object OnSwapCurrenciesClicked : ExchangeEvent()

    /**
     * User event to set the user's estimated max balance in [ExchangeModel.sourceAmountInput]
     * in [ExchangeModel.sourceCurrencyCode].
     */
    object OnMaxAmountClicked : ExchangeEvent()

    object OnMinAmountClicked : ExchangeEvent()

    /**
     * Feedback event for [ExchangeEffect.LoadWalletBalances] to set [ExchangeModel.cryptoBalances].
     */
    data class OnWalletBalancesLoaded(
        val balances: Map<String, Double>,
        val formattedCryptoBalances: Map<String, String>,
    ) : ExchangeEvent()

    /**
     * Feedback event for [ExchangeEffect.CreateOrder] or when an order is restored.
     */
    data class OnOrderUpdated(val order: ExchangeOrder) : ExchangeEvent()

    /**
     * Feedback event for [ExchangeEffect.CreateOrder].
     */
    data class OnOrderFailed(
        val type: ExchangeOrderResult.ErrorType?,
        val message: String?,
    ) : ExchangeEvent()

    /**
     * Feedback event for [ExchangeEffect.ProcessUserAction].
     */
    data class OnBrowserActionCompleted(
        val action: ExchangeOrder.Action,
        val cancelled: Boolean,
    ) : ExchangeEvent()

    /**
     * Feedback event for [ExchangeEffect.ProcessUserAction].
     */
    data class OnCryptoSendActionCompleted(
        val action: ExchangeOrder.Action,
        val transactionHash: String?,
        val cancelled: Boolean,
    ) : ExchangeEvent()

    data class OnCryptoSendActionFailed(
        val reason: SendFailedReason
    ) : ExchangeEvent()

    object OnCryptoSendHashUpdateSuccess : ExchangeEvent()
    object OnCryptoSendHashUpdateFailed : ExchangeEvent()

    sealed class SendFailedReason {
        data class InsufficientNativeWalletBalance(
            val currencyCode: String,
            val requiredAmount: Double,
        ) : SendFailedReason()
    }
}
