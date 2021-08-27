/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.exchange

import com.brd.api.models.*
import com.brd.api.models.ExchangeOfferRequest.Status
import com.brd.api.models.ExchangeOrder.Action.Type.BROWSER
import com.brd.api.models.ExchangeOrder.Action.Type.CRYPTO_SEND
import com.brd.exchange.ExchangeEvent.*
import com.brd.exchange.ExchangeModel.*
import com.brd.util.Formatters
import kt.mobius.Next
import kt.mobius.Next.Companion.dispatch
import kt.mobius.Next.Companion.next
import kt.mobius.Next.Companion.noChange
import kt.mobius.Update
import kotlin.native.concurrent.SharedImmutable
import com.brd.exchange.ExchangeEffect as F
import com.brd.exchange.ExchangeEvent as E
import com.brd.exchange.ExchangeModel as M

private const val MAX_INPUT_DIGITS = 6

@SharedImmutable
private val rawDecimalFormatter = Formatters.new().apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 8
    alwaysShowDecimalSeparator = false
}

object ExchangeUpdate : Update<M, E, F> {

    override fun update(model: M, event: E): Next<M, F> {
        return when (event) {
            is OnCountriesLoaded -> onCountriesLoaded(model, event)
            is OnPairsLoaded -> onPairsLoaded(model, event)
            is OnAmountChange -> onAmountChanged(model, event)
            is OnQuoteAmountChange -> onQuoteAmountChanged(model, event)
            is OnCountryClicked -> onCountryClicked(model, event)
            is OnRegionClicked -> onRegionClicked(model, event)
            is OnCurrencyClicked -> onCurrencyClicked(model, event)
            is OnOfferRequestUpdated -> onOfferRequestUpdated(model, event)
            is OnOfferRequestError -> onOfferRequestError(model, event)
            is OnUserPreferencesLoaded -> onUserPreferencesLoaded(model, event)
            is OnSelectPairClicked -> onSelectPairClicked(model, event)
            OnSelectPairCancelClicked -> onSelectPairCancelClicked(model)
            is OnCountriesError -> onCountriesError(model, event)
            is OnPairsError -> onPairsError(model, event)
            is OnWalletBalancesLoaded -> onWalletBalancesLoaded(model, event)
            OnSwapCurrenciesClicked -> onSwapCurrenciesClicked(model)
            OnMaxAmountClicked -> onMaxAmountClicked(model)
            OnMinAmountClicked -> onMinAmountClicked(model)
            OnContinueClicked -> onContinueClicked(model)
            is OnOrderUpdated -> onOrderUpdated(model, event)
            is OnOrderFailed -> onOrderFailed(model, event)
            OnCloseSettingsClicked -> onCloseSettingsClicked(model)
            OnConfigureSettingsClicked -> onConfigureSettingsClicked(model)
            OnConfigureCountryClicked -> onConfigureOptionClicked(model, ConfigTarget.COUNTRY)
            OnConfigureRegionClicked -> onConfigureOptionClicked(model, ConfigTarget.REGION)
            OnConfigureCurrencyClicked -> onConfigureOptionClicked(model, ConfigTarget.CURRENCY)
            is OnOfferClicked -> onOfferClicked(model, event)
            is OnSelectOfferClicked -> onSelectOfferClicked(model, event)
            OnBackClicked -> onBackClicked(model)
            is OnCloseClicked -> onCloseClicked(model, event)
            is OnBrowserActionCompleted -> onBrowserActionCompleted(model, event)
            is OnCryptoSendActionCompleted -> onCryptoSendActionCompleted(model, event)
            is OnCryptoSendActionFailed -> onCryptoSendActionFailed(model, event)
            OnDialogConfirmClicked -> onDialogConfirmClicked(model)
            OnDialogCancelClicked -> onDialogCancelClicked(model)
            OnCryptoSendHashUpdateFailed -> onCryptoSendHashUpdateFailed(model)
            OnCryptoSendHashUpdateSuccess -> onCryptoSendHashUpdateSuccess(model)
            is OnOfferAmountOverridden -> onOfferAmountOverridden(model, event)
        }
    }
}

private fun onCryptoSendHashUpdateSuccess(model: M): Next<M, F> {
    return when (model.state) {
        is State.ProcessingOrder -> next(
            model.copy(
                state = State.OrderComplete(
                    order = model.state.order,
                    offerDetails = model.state.offerDetails,
                ),
            )
        )
        else -> noChange()
    }
}

private fun onCryptoSendHashUpdateFailed(model: M): Next<M, F> {
    return when (model.state) {
        is State.ProcessingOrder -> next(
            model.copy(
                errorState = ErrorState(
                    debugMessage = "Failed to post crypto transaction hash",
                    type = ErrorState.Type.OrderError,
                    isRecoverable = false,
                )
            )
        )
        else -> noChange()
    }
}

private fun onOfferAmountOverridden(model: M, event: OnOfferAmountOverridden): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup -> {
            if (model.offerState == OfferState.GATHERING && model.matchesOfferBody(event.originalBody)) {
                next(model.copy(sourceAmountInput = event.newAmount.toString()))
            } else noChange()
        }
        else -> noChange()
    }
}

private fun onPairsLoaded(model: M, event: OnPairsLoaded): Next<M, F> {
    return when (model.state) {
        is State.Initializing,
        is State.ConfigureSettings,
        is State.OrderSetup -> {
            if (model.mode == Mode.BUY && !model.settingsOnly && !event.pairs.containsBuyPairs(model)) {
                return next(
                    model.copy(
                        errorState = ErrorState(
                            debugMessage = "No pairs for ${model.selectedCountry?.name} ${model.selectedRegion?.name}",
                            type = ErrorState.Type.UnsupportedRegionError,
                            isRecoverable = true,
                        )
                    )
                )
            }

            val currencies = if (model.test) {
                event.currencies.mapValues { (_, currency) ->
                    val currencyId = currency.currencyId
                    currency.copy(
                        currencyId = currencyId
                            .replace("ethereum-mainnet", "ethereum-ropsten")
                            .replace("mainnet", "testnet")
                    )
                }
            } else event.currencies
            val newModel = model.copy(
                state = if (model.state is State.Initializing) State.OrderSetup() else model.state,
                currencies = currencies,
                pairs = event.pairs,
                formattedFiatRates = event.formattedFiatRates,
            )
            val (source, quote) = if (model.sourceCurrencyCode == null || model.quoteCurrencyCode == null) {
                newModel.getDefaultCurrencyCodes()
            } else {
                model.sourceCurrencyCode to model.quoteCurrencyCode
            }
            val nextModel = newModel.copy(
                sourceCurrencyCode = source,
                quoteCurrencyCode = quote,
            )
            next(
                nextModel,
                setOfNotNull(
                    F.RequestOffers(nextModel.offerBodyOrNull(), model.mode),
                    nextModel.selectedFiatCurrency?.code?.let { fiatCode ->
                        F.LoadWalletBalances(fiatCode, event.pairs)
                    },
                )
            )
        }
        else -> noChange()
    }
}

private fun onPairsError(model: M, event: OnPairsError): Next<M, F> {
    return when (model.state) {
        is State.Initializing,
        is State.ConfigureSettings,
        is State.OrderSetup -> next(
            model.copy(
                errorState = ErrorState(
                    debugMessage = "${event.error.status}: ${event.error.body}",
                    type = ErrorState.Type.NetworkError,
                    isRecoverable = true,
                )
            )
        )
        else -> noChange()
    }
}

private fun onAmountChanged(model: M, event: OnAmountChange, quoteInput: Boolean = false): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup -> {
            val rawAmountInput = if (quoteInput) {
                model.quoteAmountInput ?: model.quoteAmount?.toString() ?: "0"
            } else model.sourceAmountInput
            val baseCurrency = checkNotNull(model.currencies[model.sourceCurrencyCode])
            val hasDecimal = rawAmountInput.contains(".")
            val nextAmount = when (event) {
                OnAmountChange.Decimal -> {
                    if (hasDecimal || baseCurrency.decimals == 0) {
                        rawAmountInput // no change
                    } else {
                        "$rawAmountInput."
                    }
                }
                OnAmountChange.Delete -> rawAmountInput.dropLast(1).ifEmpty { "0" }
                OnAmountChange.Clear -> "0"
                is OnAmountChange.Digit -> {
                    val baseLength = rawAmountInput.substringBefore('.').length
                    val fractionDigits = rawAmountInput.substringAfter('.', "").length
                    val hasMaxDigits = baseLength == MAX_INPUT_DIGITS
                    val hasMaxDecimalDigits = fractionDigits == baseCurrency.decimals
                    when {
                        hasMaxDigits && !hasDecimal && !hasMaxDecimalDigits -> {
                            rawAmountInput.dropLast(1) + event.digit
                        }
                        hasMaxDigits && hasMaxDecimalDigits -> {
                            rawAmountInput.dropLast(1) + event.digit
                        }
                        hasDecimal && hasMaxDecimalDigits -> {
                            rawAmountInput.dropLast(1) + event.digit
                        }
                        rawAmountInput == "0" -> event.digit.toString()
                        else -> rawAmountInput + event.digit
                    }
                }
            }
            val isErrorInput = rawAmountInput.length == nextAmount.length
                    || nextAmount.toDoubleOrNull() == null
            val inputError = when (model.mode) {
                Mode.BUY -> null
                Mode.SELL, Mode.TRADE -> if (quoteInput) {
                    null
                } else {
                    val walletBalance = model.cryptoBalances[model.sourceCurrencyCode] ?: 0.0
                    if (nextAmount.toDouble() > walletBalance) {
                        InputError.BalanceLow(walletBalance)
                    } else null
                }
            }
            val newSourceAmount = if (quoteInput) {
                (model.selectedPair?.inputFromOutput(nextAmount.toDouble()) ?: 0).toString()
            } else nextAmount
            val newQuoteAmount = if (quoteInput) nextAmount else null
            val newModel = model.copy(
                sourceAmountInput = newSourceAmount,
                quoteAmountInput = newQuoteAmount,
                offerRequest = null,
                offerDetails = emptyList(),
                inputError = inputError,
                selectedOffer = null,
                lastOfferSelection = null,
            )
            next(
                newModel,
                setOfNotNull(
                    F.RequestOffers(newModel.offerBodyOrNull(), model.mode),
                    if (nextAmount.length > 1 && isErrorInput) F.ErrorSignal else null,
                )
            )
        }
        else -> noChange()
    }
}

private fun onQuoteAmountChanged(model: M, event: OnQuoteAmountChange): Next<M, F> {
    return onAmountChanged(model, event.amountChange, quoteInput = true)
}

private fun onCountriesLoaded(model: M, event: OnCountriesLoaded): Next<M, F> {
    return when (model.state) {
        is State.Initializing -> {
            val selectedCountry = event.countries
                .find { it.code == event.defaultCountryCode }
            val selectedRegion =
                selectedCountry?.regions?.find { it.code == event.defaultRegionCode }
            val sortedRegionCountry = selectedCountry?.copy(
                regions = (listOfNotNull(selectedRegion) + selectedCountry.regions)
                    .distinct()
            )
            val sortedCountries = (listOfNotNull(sortedRegionCountry) + event.countries)
                .distinctBy(ExchangeCountry::code)
            val newModel = model.copy(
                countries = sortedCountries,
                selectedCountry = selectedCountry,
                selectedRegion = selectedRegion,
                selectedFiatCurrency = model.selectedFiatCurrency ?: selectedCountry?.currency
            )
            next(
                newModel,
                setOfNotNull(
                    F.LoadUserPreferences,
                )
            )
        }
        else -> noChange()
    }
}

private fun onCountriesError(model: M, event: OnCountriesError): Next<M, F> {
    return when (model.state) {
        is State.Initializing -> next(
            model.copy(
                errorState = ErrorState(
                    debugMessage = "${event.error.status} : ${event.error.body}",
                    isRecoverable = true,
                    type = ErrorState.Type.NetworkError,
                )
            )
        )
        else -> noChange()
    }
}

private fun onCurrencyClicked(model: M, event: OnCurrencyClicked): Next<M, F> {
    return when (model.state) {
        is State.ConfigureSettings -> {
            val trackingEffect = F.TrackEvent(model.event("change_currency"))
            next(
                model.copy(
                    state = model.state.copy(target = ConfigTarget.MENU),
                    selectedFiatCurrency = event.currency,
                    sourceCurrencyCode = when (model.mode) {
                        Mode.BUY -> event.currency.code
                        else -> model.sourceCurrencyCode
                    },
                    quoteCurrencyCode = when (model.mode) {
                        Mode.SELL -> event.currency.code
                        else -> model.quoteCurrencyCode
                    }
                ),
                if (model.settingsOnly) {
                    setOfNotNull(
                        trackingEffect,
                        F.UpdateCurrencyPreference(event.currency.code),
                    )
                } else {
                    setOfNotNull(
                        trackingEffect,
                        F.LoadWalletBalances(
                            event.currency.code,
                            model.pairs,
                        ),
                        if (model.mode == Mode.TRADE) null else {
                            val country = checkNotNull(model.selectedCountry)
                            F.LoadPairs(country.code, model.selectedRegion?.code, event.currency.code)
                        }
                    )
                }
            )
        }
        is State.SelectAsset -> {
            val trackingEffect = F.TrackEvent(model.event("change_currency"))
            if (model.state.source) {
                val sourcePairs = model.pairs.filter { pair ->
                    val source = model.currencies.getValue(pair.fromCode)
                    val quote = model.currencies.getValue(pair.toCode)
                    pair.fromCode == event.currency.code &&
                            model.mode.isCompatibleSource(source) &&
                            model.mode.isCompatibleQuote(quote)
                }
                val newQuoteCurrencyCode = model.quoteCurrencyCode?.run {
                    sourcePairs.find { it.toCode == model.quoteCurrencyCode }?.toCode
                }
                val newState = if (newQuoteCurrencyCode == null) {
                    State.SelectAsset(
                        source = false,
                        assets = sourcePairs.map { pair ->
                            model.currencies.getValue(pair.toCode)
                        }
                    )
                } else State.OrderSetup()
                val newModel = model.copy(
                    sourceCurrencyCode = event.currency.code,
                    quoteCurrencyCode = newQuoteCurrencyCode,
                    offerRequest = null,
                    offerDetails = emptyList(),
                    selectedOffer = null,
                    state = newState,
                )
                next(
                    newModel,
                    setOfNotNull(
                        F.RequestOffers(newModel.offerBodyOrNull(), model.mode),
                        trackingEffect,
                    )
                )
            } else {
                val newModel = model.copy(
                    state = State.OrderSetup(),
                    quoteCurrencyCode = event.currency.code,
                    offerRequest = null,
                    offerDetails = emptyList(),
                    selectedOffer = null,
                )
                next(
                    newModel,
                    setOfNotNull(
                        F.RequestOffers(newModel.offerBodyOrNull(), model.mode),
                        trackingEffect,
                        if (model.mode == Mode.BUY) F.UpdateLastOrderCurrency(event.currency.code) else null
                    )
                )
            }
        }
        else -> noChange()
    }
}

private fun onRegionClicked(model: M, event: OnRegionClicked): Next<M, F> {
    return when (model.state) {
        is State.ConfigureSettings -> {
            val showMenu = model.selectedCountry?.currency?.code == model.selectedFiatCurrency?.code
            val target = if (showMenu) ConfigTarget.MENU else ConfigTarget.CURRENCY
            next(
                model.copy(
                    selectedRegion = event.region,
                    state = model.state.copy(target = target)
                ),
                setOfNotNull(
                    model.selectedCountry?.code?.let { countryCode ->
                        F.UpdateRegionPreferences(countryCode, event.region.code)
                    },
                    if (model.settingsOnly || model.selectedCountry == null) {
                        null
                    } else {
                        F.LoadPairs(
                            model.selectedCountry.code,
                            event.region.code,
                            model.selectedFiatCurrency?.code
                        )
                    }
                )
            )
        }
        else -> noChange()
    }
}

private fun onCountryClicked(model: M, event: OnCountryClicked): Next<M, F> {
    return when (model.state) {
        is State.ConfigureSettings -> {
            val country = event.country
            val preselectedRegion = country.regions.firstOrNull()
            val showMenu = model.selectedCountry?.currency?.code == model.selectedFiatCurrency?.code
            val target = when {
                country.regions.isEmpty() -> {
                    if (showMenu) ConfigTarget.MENU else ConfigTarget.CURRENCY
                }
                else -> ConfigTarget.REGION
            }
            val selectedFiatCurrency = model.selectedFiatCurrency ?: country.currency
            next(
                model.copy(
                    selectedCountry = country,
                    selectedRegion = preselectedRegion,
                    selectedFiatCurrency = selectedFiatCurrency,
                    state = model.state.copy(target = target)
                ),
                setOfNotNull(
                    F.UpdateRegionPreferences(country.code, preselectedRegion?.code),
                    if (model.settingsOnly || country.regions.isNotEmpty()) {
                        null
                    } else {
                        F.LoadPairs(country.code, null, selectedFiatCurrency.code)
                    }
                )
            )
        }
        else -> noChange()
    }
}

private fun onUserPreferencesLoaded(model: M, event: OnUserPreferencesLoaded): Next<M, F> {
    val isNewUser = event.selectedCountryCode == null
    val fiatCurrencies = model.countries.map(ExchangeCountry::currency).distinct()
    val selectedCountry = model.countries.find { it.code == event.selectedCountryCode }
    val selectedRegion =
        selectedCountry?.regions?.find { it.code == event.selectedRegionCode }
    val selectedFiatCurrency = model.countries.find {
        it.currency.code.equals(event.fiatCurrencyCode, true)
    }?.currency
    if (model.settingsOnly) {
        return next(
            model.copy(
                state = State.ConfigureSettings(
                    target = ConfigTarget.MENU,
                    isNewUser = isNewUser,
                    fiatCurrencies = fiatCurrencies,
                ),
                selectedFiatCurrency = selectedFiatCurrency,
                selectedCountry = selectedCountry,
                selectedRegion = selectedRegion,
            )
        )
    }
    return when (model.state) {
        is State.Initializing -> if (isNewUser) {
            next(
                model.copy(
                    apiHost = event.apiHost,
                    state = State.ConfigureSettings(
                        target = ConfigTarget.MENU,
                        isNewUser = true,
                        fiatCurrencies = fiatCurrencies,
                    )
                ),
                setOfNotNull(
                    model.selectedCountry?.let {
                        F.LoadPairs(it.code, model.selectedRegion?.code, model.selectedFiatCurrency?.code)
                    }
                )
            )
        } else {
            val newModel = model.copy(
                apiHost = event.apiHost,
                sourceAmountInput = (if (model.mode == Mode.TRADE) null else event.lastOrderAmount) ?: "0",
                selectedFiatCurrency = selectedFiatCurrency,
                selectedCountry = selectedCountry,
                selectedRegion = selectedRegion,
                lastPurchaseCurrencyCode = event.lastPurchaseCurrencyCode ?: model.lastPurchaseCurrencyCode,
                lastTradeSourceCurrencyCode = event.lastTradeSourceCurrencyCode
                    ?: model.lastTradeSourceCurrencyCode,
                lastTradeQuoteCurrencyCode = event.lastTradeQuoteCurrencyCode ?: model.lastTradeQuoteCurrencyCode,
            )
            next(
                newModel,
                setOfNotNull(
                    selectedCountry?.let {
                        F.LoadPairs(selectedCountry.code, selectedRegion?.code, selectedFiatCurrency?.code)
                    },
                    F.RequestOffers(model.offerBodyOrNull(), model.mode),
                )
            )
        }
        else -> noChange()
    }
}

private fun onSelectPairClicked(model: M, event: OnSelectPairClicked): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup,
        is State.SelectAsset -> {
            val currenciesSlice = if (event.selectSource) {
                model.pairs
                    .distinctBy(ExchangePair::fromCode)
                    .map(ExchangePair::fromCode)
                    .run {
                        if (model.mode == Mode.TRADE) {
                            filter { (model.cryptoBalances[it] ?: 0.0) > 0 }
                        } else this
                    }
            } else {
                model.sourcePairs.map(ExchangePair::toCode)
            }.mapNotNull { code ->
                model.currencies.getValue(code)
                    .takeIf { currency ->
                        if (event.selectSource) {
                            model.mode.isCompatibleSource(currency)
                        } else {
                            model.mode.isCompatibleQuote(currency)
                        }
                    }
            }
            next(
                model.copy(
                    state = State.SelectAsset(
                        source = event.selectSource,
                        assets = currenciesSlice,
                    )
                )
            )
        }
        else -> noChange()
    }
}

private fun onSelectPairCancelClicked(model: M): Next<M, F> {
    return when (model.state) {
        is State.SelectAsset -> next(
            model.copy(
                state = State.OrderSetup(),
                quoteCurrencyCode = model.quoteCurrencyCode
                    ?: model.sourcePairs.firstOrNull()?.toCode
            )
        )
        else -> noChange()
    }
}

private fun onOfferRequestUpdated(model: M, event: OnOfferRequestUpdated): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup -> {
            if (model.matchesOfferBody(event.offerBody)) {
                // Retain current selection or select default
                val selectedOffer = if (model.lastOfferSelection == null) {
                    model.selectedOffer ?: event.offerDetails.getDefaultOffer(model.mode)
                } else {
                    val lastOffer = model.lastOfferSelection.offer
                    event.offerDetails.find { details ->
                        details.offer.provider.slug == lastOffer.provider.slug &&
                                details.offer.sourceCurrencyMethod::class == lastOffer.sourceCurrencyMethod::class
                    }
                }
                val orderedOfferDetails = selectedOffer?.let { listOf(it) + (event.offerDetails - it) }
                val isGatheringOffers = event.exchangeOfferRequest.status == Status.GATHERING
                next(
                    model.copy(
                        selectedOffer = selectedOffer,
                        offerRequest = event.exchangeOfferRequest,
                        offerDetails = orderedOfferDetails ?: event.offerDetails,
                        lastOfferSelection = if (selectedOffer == null && isGatheringOffers) {
                            model.lastOfferSelection
                        } else null,
                    )
                )
            } else noChange()
        }
        else -> noChange()
    }
}

private fun onOfferRequestError(model: M, event: OnOfferRequestError): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup -> {
            if (model.matchesOfferBody(event.offerBody)) {
                next(
                    model.copy(
                        offerRequest = null,
                        offerDetails = emptyList(),
                        selectedOffer = null,
                        lastOfferSelection = null,
                        errorState = ErrorState(
                            debugMessage = "${event.error.status} : ${event.error.body}",
                            isRecoverable = true,
                            type = ErrorState.Type.NetworkError,
                        )
                    )
                )
            } else noChange()
        }
        else -> noChange()
    }
}

private fun onSwapCurrenciesClicked(model: M): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup -> {
            val hasOppositePair = model.pairs.any {
                it.toCode == model.sourceCurrencyCode &&
                        it.fromCode == model.quoteCurrencyCode
            }
            if (hasOppositePair) {
                val newModel = model.copy(
                    sourceCurrencyCode = model.quoteCurrencyCode,
                    quoteCurrencyCode = model.sourceCurrencyCode,
                    offerRequest = null,
                    offerDetails = emptyList(),
                    selectedOffer = null,
                    lastOfferSelection = null,
                    sourceAmountInput = model.formattedQuoteAmount
                        ?.filter { it.isDigit() || it == '.' } ?: "0",
                    quoteAmountInput = null,
                    mode = when (model.mode) {
                        Mode.BUY -> Mode.SELL
                        Mode.SELL -> Mode.BUY
                        Mode.TRADE -> Mode.TRADE
                    }
                )
                next(
                    newModel,
                    F.RequestOffers(newModel.offerBodyOrNull(), model.mode)
                )
            } else noChange()
        }
        else -> noChange()
    }
}

private fun onWalletBalancesLoaded(model: M, event: OnWalletBalancesLoaded): Next<M, F> {
    val sortedBalances = event.balances.toList()
        .sortedByDescending { (_, balance) -> balance }
        .toMap()
    val greatestBalance = sortedBalances.values.firstOrNull() ?: 0.0
    val inConfig = model.state is State.ConfigureSettings
    val nextModel = model.copy(
        state = if (model.mode == Mode.BUY || greatestBalance > 0 || inConfig) model.state else State.EmptyWallets,
        cryptoBalances = sortedBalances,
        formattedCryptoBalances = event.formattedCryptoBalances,
    )
    return if (nextModel.quoteAmount == null) {
        val (source, quote) = nextModel.getDefaultCurrencyCodes()
        next(
            nextModel.copy(
                sourceCurrencyCode = source,
                quoteCurrencyCode = quote,
            )
        )
    } else next(nextModel)
}

private fun onMaxAmountClicked(model: M): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup -> when (model.mode) {
            Mode.SELL, Mode.TRADE -> {
                val balance = model.sourceCurrencyCode?.run(model.cryptoBalances::get) ?: 0.0
                val newModel = model.copy(
                    sourceAmountInput = rawDecimalFormatter.format(balance),
                    quoteAmountInput = null,
                    offerRequest = null,
                    offerDetails = emptyList(),
                    inputError = null,
                    selectedOffer = null,
                    lastOfferSelection = null,
                )
                next(
                    newModel,
                    F.TrackEvent(model.event("set_max")),
                    F.RequestOffers(newModel.offerBodyOrNull(), model.mode),
                )
            }
            else -> noChange()
        }
        else -> noChange()
    }
}

private fun onMinAmountClicked(model: M): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup -> when (model.mode) {
            Mode.SELL, Mode.TRADE -> {
                dispatch(F.TrackEvent(model.event("set_min")))
            }
            else -> noChange()
        }
        else -> noChange()
    }
}

private fun onContinueClicked(model: M): Next<M, F> {
    return when (model.state) {
        is State.ConfigureSettings -> when (model.state.target) {
            ConfigTarget.MENU -> when {
                model.settingsOnly -> dispatch(F.ExitFlow)
                model.isRegionConfigured() -> {
                    val currencyCode = checkNotNull(model.selectedFiatCurrency).code
                    val countryCode = checkNotNull(model.selectedCountry).code
                    val regionCode = model.selectedRegion?.code
                    next(
                        model.copy(
                            state = State.OrderSetup(),
                        ),
                        setOf(
                            F.UpdateCurrencyPreference(currencyCode),
                            F.UpdateRegionPreferences(countryCode, regionCode),
                            F.LoadPairs(countryCode, regionCode, currencyCode)
                        )
                    )
                }
                else -> noChange()
            }
            ConfigTarget.COUNTRY -> if (model.selectedCountry == null) {
                noChange()
            } else {
                next(
                    model.copy(
                        state = model.state.copy(
                            target = if (model.selectedCountry.regions.isEmpty()) {
                                ConfigTarget.CURRENCY
                            } else {
                                ConfigTarget.REGION
                            }
                        )
                    ),
                    setOfNotNull(
                        model.selectedCountry.run {
                            F.LoadPairs(code, model.selectedRegion?.code, model.selectedFiatCurrency?.code)
                        },
                        model.selectedCountry.run {
                            F.UpdateRegionPreferences(code, model.selectedRegion?.code)
                        },
                    )
                )
            }
            ConfigTarget.REGION -> if (model.selectedRegion == null) {
                noChange()
            } else {
                next(
                    model.copy(
                        state = model.state.copy(
                            target = ConfigTarget.CURRENCY
                        )
                    ),
                    setOfNotNull(
                        model.selectedCountry?.run {
                            F.LoadPairs(code, model.selectedRegion.code, model.selectedFiatCurrency?.code)
                        },
                        model.selectedCountry?.run {
                            F.UpdateRegionPreferences(code, model.selectedRegion.code)
                        },
                    )
                )
            }
            ConfigTarget.CURRENCY -> {
                val fiatCurrencyCode = model.selectedFiatCurrency?.code
                if (fiatCurrencyCode.isNullOrBlank()) {
                    noChange()
                } else {
                    next(
                        model.copy(
                            state = if (model.state.isNewUser && !model.settingsOnly) {
                                State.OrderSetup()
                            } else {
                                model.state.copy(target = ConfigTarget.MENU)
                            },
                            quoteCurrencyCode = model.quoteCurrencyCode
                                ?: model.getDefaultCurrencyCodes().second
                        ),
                        setOfNotNull(
                            F.UpdateCurrencyPreference(fiatCurrencyCode),
                        )
                    )
                }
            }
        }
        is State.EmptyWallets -> {
            next(
                M.create(Mode.BUY, model.test),
                setOf(
                    F.LoadCountries,
                    F.TrackEvent(model.event("appeared")),
                )
            )
        }
        is State.OrderSetup -> when {
            model.selectedOffer == null -> noChange()
            model.inputError != null -> noChange()
            model.selectedOffer is OfferDetails.InvalidOffer -> {
                val replacementAmount = model.selectedOffer.rawReplacementAmount
                if (replacementAmount == null) {
                    dispatch(F.ErrorSignal)
                } else {
                    val newModel = model.copy(
                        selectedOffer = null,
                        offerRequest = null,
                        offerDetails = emptyList(),
                        sourceAmountInput = replacementAmount,
                        quoteAmountInput = null,
                        lastOfferSelection = model.selectedOffer,
                    )
                    next(
                        newModel,
                        F.RequestOffers(newModel.offerBodyOrNull(), model.mode)
                    )
                }
            }
            else -> {
                val requiresPreview = model.mode == Mode.TRADE
                next(
                    model.copy(
                        state = State.CreatingOrder(previewing = requiresPreview)
                    ),
                    setOfNotNull(
                        if (requiresPreview) {
                            F.UpdateLastTradeCurrencyPair(
                                sourceCode = model.sourceCurrencyCode,
                                quoteCode = model.quoteCurrencyCode
                            )
                        } else null,
                        if (requiresPreview) null else F.CreateOrder(offer = model.selectedOffer.offer),
                    )
                )
            }
        }
        is State.CreatingOrder -> when {
            model.selectedOffer == null -> noChange()
            model.state.previewing -> next(
                model.copy(state = model.state.copy(previewing = false)),
                F.CreateOrder(offer = model.selectedOffer.offer)
            )
            else -> noChange()
        }
        is State.ProcessingOrder -> noChange()
        is State.OrderComplete -> {
            if (model.mode == Mode.TRADE) {
                dispatch(F.ExitFlow) // Exiting to Home on trade continue completed to match iOS
            } else {
                next(
                    model.copy(
                        state = State.OrderSetup(),
                    ),
                    F.LoadPairs(
                        countryCode = checkNotNull(model.selectedCountry).code,
                        regionCode = model.selectedRegion?.code,
                        selectedFiatCurrencyCode = model.selectedFiatCurrency?.code
                    )
                )
            }
        }
        else -> noChange()
    }
}

private fun onOrderUpdated(model: M, event: OnOrderUpdated): Next<M, F> {
    return when (model.state) {
        is State.CreatingOrder -> {
            val selectedOffer = checkNotNull(model.selectedOffer)
            check(selectedOffer is OfferDetails.ValidOffer)

            val newModel = model.copy(
                state = State.ProcessingOrder(event.order, selectedOffer),
                offerRequest = null,
                offerDetails = emptyList(),
                selectedOffer = null,
                lastOfferSelection = null,
            )
            val eventProps = newModel.createOrderEventProps()
            next(
                newModel,
                setOf(
                    F.ProcessBackgroundActions(event.order),
                    F.TrackEvent("checkout", eventProps)
                )
            )
        }
        is State.ProcessingOrder -> {
            when (event.order.status) {
                ExchangeOrder.Status.INITIALIZING,
                ExchangeOrder.Status.INITIALIZED -> {
                    val outputUserActions = event.order.outputs
                        .flatMap { output -> output.actions }
                        .filter { it.type == BROWSER }
                    val inputUserActions = event.order.inputs
                        .flatMap { input -> input.actions }
                        .filter { action ->
                            (action.type == BROWSER || action.type == CRYPTO_SEND)
                        }

                    val userAction = (outputUserActions + inputUserActions).firstOrNull()
                    val effect = userAction?.let {
                        F.ProcessUserAction(
                            order = event.order,
                            baseUrl = model.apiHost.host,
                            action = userAction,
                        )
                    }

                    next(
                        model.copy(
                            state = model.state.copy(
                                order = event.order,
                                userAction = effect,
                            )
                        ),
                        setOfNotNull(effect)
                    )
                }
                ExchangeOrder.Status.FINALIZED -> {
                    val eventProps = model.createOrderEventProps()
                    next(
                        model.copy(
                            state = State.OrderComplete(
                                event.order,
                                model.state.offerDetails,
                            ),
                        ),
                        setOf(
                            F.TrackEvent("complete", eventProps)
                        )
                    )
                }
            }
        }
        else -> noChange()
    }
}

private fun onOrderFailed(model: M, event: OnOrderFailed): Next<M, F> {
    val eventProps = model.createOrderEventProps(event.type?.name)
    val trackingEffect = F.TrackEvent(model.event("fail"), eventProps)
    return when (model.state) {
        is State.CreatingOrder -> {
            next(
                model.copy(
                    state = model.state.copy(previewing = true),
                    errorState = ErrorState(
                        type = ErrorState.Type.OrderError,
                        isRecoverable = false,
                        debugMessage = event.toString()
                    ),
                ),
                setOf(trackingEffect)
            )
        }
        is State.ProcessingOrder -> {
            next(
                model.copy(
                    state = State.OrderSetup(),
                    errorState = ErrorState(
                        type = ErrorState.Type.OrderError,
                        isRecoverable = false,
                        debugMessage = event.toString()
                    ),
                ),
                setOf(trackingEffect)
            )
        }
        else -> noChange()
    }
}

private fun onCloseSettingsClicked(model: M): Next<M, F> {
    return when (model.state) {
        is State.ConfigureSettings ->
            when (model.state.target) {
                ConfigTarget.MENU -> if (model.isRegionConfigured() && !model.settingsOnly) {
                    next(
                        model.copy(
                            state = State.OrderSetup()
                        )
                    )
                } else dispatch(F.ExitFlow)
                ConfigTarget.COUNTRY,
                ConfigTarget.REGION,
                ConfigTarget.CURRENCY -> next(
                    model.copy(
                        state = model.state.copy(
                            target = ConfigTarget.MENU
                        )
                    )
                )
            }
        else -> noChange()
    }
}

private fun onConfigureSettingsClicked(model: M): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup -> next(
            model.copy(
                state = State.ConfigureSettings(
                    target = ConfigTarget.MENU,
                    isNewUser = false,
                    fiatCurrencies = model.countries.map(ExchangeCountry::currency).distinct(),
                )
            )
        )
        else -> noChange()
    }
}

private fun onConfigureOptionClicked(model: M, target: ConfigTarget): Next<M, F> {
    return when (model.state) {
        is State.ConfigureSettings -> if (model.state.target == target) {
            noChange()
        } else {
            next(model.copy(state = model.state.copy(target = target)))
        }
        else -> noChange()
    }
}

private fun onOfferClicked(model: M, event: OnOfferClicked): Next<M, F> {
    return when (model.state) {
        is State.OrderSetup -> if (model.state.selectingOffer) {
            val adjustedAmount = if (event.adjustToLimit && event.offerDetails is OfferDetails.InvalidOffer) {
                event.offerDetails.rawReplacementAmount
            } else null
            val isAdjusted = { _: Any? -> adjustedAmount != null }
            val newModel = model.copy(
                state = State.OrderSetup(selectingOffer = false),
                offerRequest = model.offerRequest.takeUnless(isAdjusted),
                selectedOffer = event.offerDetails.takeUnless(isAdjusted),
                offerDetails = model.offerDetails.takeUnless(isAdjusted).orEmpty(),
                sourceAmountInput = adjustedAmount ?: model.sourceAmountInput,
                quoteAmountInput = null,
                lastOfferSelection = (event.offerDetails as? OfferDetails.InvalidOffer).takeIf(isAdjusted),
            )
            val requestOffers = adjustedAmount?.run {
                F.RequestOffers(newModel.offerBodyOrNull(), model.mode)
            }
            next(newModel, setOfNotNull(requestOffers))
        } else noChange()
        else -> noChange()
    }
}

private fun onSelectOfferClicked(model: M, event: OnSelectOfferClicked): Next<M, F> {
    if (model.offerDetails.size <= 1) return noChange()
    return when (model.state) {
        is State.OrderSetup -> if (model.offerState == OfferState.COMPLETED) {
            next(
                model.copy(
                    state = State.OrderSetup(selectingOffer = !event.cancel),
                )
            )
        } else noChange()
        else -> noChange()
    }
}

private fun onBackClicked(model: M): Next<M, F> {
    if (model.confirmingClose) return next(model.copy(confirmingClose = false))

    return when (model.state) {
        is State.SelectAsset -> next(model.copy(state = State.OrderSetup()))
        is State.CreatingOrder -> if (model.state.previewing) {
            next(model.copy(state = State.OrderSetup()))
        } else {
            next(model.copy(confirmingClose = true))
        }
        is State.ProcessingOrder -> next(model.copy(confirmingClose = true))
        is State.Initializing,
        is State.EmptyWallets,
        is State.OrderComplete -> dispatch(F.ExitFlow)
        is State.OrderSetup -> if (model.state.selectingOffer) {
            next(
                model.copy(
                    state = model.state.copy(
                        selectingOffer = false
                    )
                )
            )
        } else {
            dispatch(F.ExitFlow)
        }
        is State.ConfigureSettings -> if (model.state.isNewUser || model.settingsOnly) {
            when (model.state.target) {
                ConfigTarget.MENU -> dispatch(F.ExitFlow)
                ConfigTarget.CURRENCY,
                ConfigTarget.REGION,
                ConfigTarget.COUNTRY -> next(
                    model.copy(
                        state = model.state.copy(
                            target = ConfigTarget.MENU
                        )
                    )
                )
            }
        } else {
            when (model.state.target) {
                ConfigTarget.MENU -> next(model.copy(state = State.OrderSetup()))
                ConfigTarget.REGION,
                ConfigTarget.COUNTRY,
                ConfigTarget.CURRENCY -> next(
                    model.copy(
                        state = model.state.copy(
                            target = ConfigTarget.MENU
                        )
                    )
                )
            }
        }
    }
}

private fun onCloseClicked(model: M, event: OnCloseClicked): Next<M, F> {
    if (model.confirmingClose) {
        return if (event.confirmed) {
            next(
                model.copy(state = State.OrderSetup(), confirmingClose = false),
                F.RequestOffers(model.offerBodyOrNull(), model.mode)
            )
        } else {
            next(model.copy(confirmingClose = false))
        }
    }
    return when (model.state) {
        is State.CreatingOrder -> if (model.state.previewing) {
            dispatch(F.ExitFlow)
        } else {
            next(model.copy(confirmingClose = true))
        }
        is State.ConfigureSettings -> when (model.state.target) {
            ConfigTarget.MENU -> {
                if (model.state.isNewUser || model.settingsOnly) {
                    dispatch(F.ExitFlow)
                } else {
                    next(model.copy(state = State.OrderSetup()))
                }
            }
            ConfigTarget.REGION,
            ConfigTarget.COUNTRY,
            ConfigTarget.CURRENCY -> next(
                model.copy(
                    state = model.state.copy(
                        target = ConfigTarget.MENU
                    )
                )
            )
        }
        is State.SelectAsset -> next(model.copy(state = State.OrderSetup()))
        is State.ProcessingOrder -> next(model.copy(confirmingClose = true))
        is State.OrderSetup -> if (model.state.selectingOffer) {
            next(model.copy(state = model.state.copy(selectingOffer = false)))
        } else {
            dispatch(F.ExitFlow)
        }
        is State.OrderComplete,
        is State.Initializing,
        is State.EmptyWallets -> dispatch(F.ExitFlow)
    }
}

private fun onBrowserActionCompleted(model: M, event: OnBrowserActionCompleted): Next<M, F> {
    return when (model.state) {
        is State.ProcessingOrder -> {
            val newModel = model.copy(
                state = State.OrderSetup()
            )
            next(
                newModel,
                setOfNotNull(
                    if (model.state.userAction?.action == event.action) {
                        F.UpdateLastOrderAmount(model.sourceAmountInput)
                    } else null,
                    F.RequestOffers(newModel.offerBodyOrNull(), newModel.mode),
                )
            )
        }
        else -> noChange()
    }
}


private fun onCryptoSendActionCompleted(model: M, event: OnCryptoSendActionCompleted): Next<M, F> {
    return when (model.state) {
        is State.ProcessingOrder -> when {
            model.state.userAction == null -> noChange()
            event.cancelled -> next(
                model.copy(
                    state = State.OrderSetup()
                ),
                setOfNotNull(
                    F.RequestOffers(model.offerBodyOrNull(), model.mode)
                )
            )
            event.transactionHash.isNullOrBlank() ->
                next(
                    model.copy(
                        state = model.state.copy(
                            userAction = null,
                        ),
                        errorState = ErrorState(
                            debugMessage = "Missing transaction hash",
                            type = ErrorState.Type.TransactionError(),
                            isRecoverable = false,
                        )
                    )
                )
            else -> next(
                model.copy(
                    state = model.state.copy(
                        userAction = null,
                    )
                ),
                F.SubmitCryptoTransferHash(
                    model.state.order,
                    event.action,
                    event.transactionHash,
                )
            )
        }
        else -> noChange()
    }
}

private fun onCryptoSendActionFailed(model: M, event: OnCryptoSendActionFailed): Next<M, F> {
    return when (model.state) {
        is State.ProcessingOrder -> when (event.reason) {
            is SendFailedReason.CreateTransferFailed -> next(
                model.copy(
                    errorState = ErrorState(
                        debugMessage = "Failed to create transfer",
                        type = ErrorState.Type.TransactionError(event.reason),
                        isRecoverable = false,
                    )
                )
            )
            is SendFailedReason.FeeEstimateFailed -> next(
                model.copy(
                    errorState = ErrorState(
                        debugMessage = "Failed to estimate fee",
                        type = ErrorState.Type.TransactionError(event.reason),
                        isRecoverable = true,
                    )
                )
            )
            is SendFailedReason.InsufficientNativeWalletBalance -> next(
                model.copy(
                    errorState = ErrorState(
                        debugMessage = "Insufficient native wallet balance: ${event.reason.requiredAmount}",
                        type = ErrorState.Type.InsufficientNativeBalanceError(
                            event.reason.currencyCode,
                            event.reason.requiredAmount,
                        ),
                        isRecoverable = true
                    )
                )
            )
        }
        else -> noChange()
    }
}

private fun onDialogConfirmClicked(model: M): Next<M, F> {
    if (model.errorState?.type == ErrorState.Type.UnsupportedRegionError) {
        return next(
            model.copy(
                state = State.ConfigureSettings(
                    target = ConfigTarget.MENU,
                    isNewUser = true,
                    fiatCurrencies = model.countries.map(ExchangeCountry::currency).distinct()
                ),
                errorState = null,
            )
        )
    }
    if (model.errorState?.type is ErrorState.Type.InsufficientNativeBalanceError) {
        val type = model.errorState.type as ErrorState.Type.InsufficientNativeBalanceError
        val newModel = model.copy(
            mode = Mode.BUY,
            errorState = null,
            state = State.OrderSetup(),
            sourceAmountInput = type.amount.toString(),
            quoteAmountInput = null,
            offerRequest = null,
            offerDetails = emptyList(),
            lastOfferSelection = null,
            selectedOffer = null,
            sourceCurrencyCode = model.selectedFiatCurrency?.code,
            quoteCurrencyCode = type.currencyCode,
        )
        return next(newModel, F.RequestOffers(newModel.offerBodyOrNull(), Mode.BUY))
    }
    val transactionError = model.errorState?.type as? ErrorState.Type.TransactionError
    if (transactionError?.sendFailedReason == SendFailedReason.FeeEstimateFailed) {
        val newModel = model.copy(
            errorState = null,
        )
        return next(newModel, setOfNotNull((model.state as State.ProcessingOrder).userAction))
    }
    return when (model.state) {
        is State.Initializing -> {
            if (model.errorState?.isRecoverable == true) {
                when {
                    model.countries.isEmpty() -> {
                        next(
                            model.copy(
                                state = State.Initializing,
                                errorState = null,
                            ),
                            F.LoadCountries,
                        )
                    }
                    model.pairs.isEmpty() && model.isRegionConfigured() -> {
                        next(
                            model.copy(
                                state = State.Initializing,
                                errorState = null,
                            ),
                            F.LoadPairs(
                                countryCode = checkNotNull(model.selectedCountry).code,
                                regionCode = model.selectedRegion?.code,
                                selectedFiatCurrencyCode = model.selectedFiatCurrency?.code,
                            ),
                        )
                    }
                    else -> noChange()
                }
            } else dispatch(F.ExitFlow)
        }
        is State.OrderSetup -> {
            if (model.errorState?.isRecoverable == true) {
                if (model.offerRequest == null) {
                    next(
                        model.copy(errorState = null),
                        F.RequestOffers(model.offerBodyOrNull(), model.mode)
                    )
                } else {
                    noChange()
                }
            } else dispatch(F.ExitFlow)
        }
        is State.CreatingOrder -> {
            next(
                model.copy(
                    state = State.OrderSetup(),
                    confirmingClose = false,
                    errorState = null,
                ),
                setOfNotNull(
                    F.RequestOffers(model.offerBodyOrNull(), model.mode)
                )
            )
        }
        is State.ProcessingOrder -> if (model.confirmingClose) {
            next(
                model.copy(
                    state = State.OrderSetup(),
                    confirmingClose = false
                ),
                setOfNotNull(
                    F.RequestOffers(model.offerBodyOrNull(), model.mode)
                )
            )
        } else noChange()
        else -> if (model.confirmingClose) dispatch(F.ExitFlow) else noChange()
    }
}


private fun onDialogCancelClicked(model: M): Next<M, F> {
    return when {
        model.confirmingClose -> next(model.copy(confirmingClose = false))
        model.errorState?.isRecoverable == true -> dispatch(F.ExitFlow)
        else -> noChange()
    }
}


// Returns the default Pair(baseCurrencyCode?, quoteCurrencyCode?) give the mode.
private fun M.getDefaultCurrencyCodes(): Pair<String?, String?> {
    return when (mode) {
        Mode.BUY -> selectedFiatCurrency?.code to lastPurchaseCurrencyCode
        Mode.SELL -> lastPurchaseCurrencyCode to selectedFiatCurrency?.code
        Mode.TRADE -> {
            // Largest wallet balance or first active wallet
            val source = lastTradeSourceCurrencyCode
                ?: cryptoBalances.maxByOrNull { (_, balance) -> balance }?.key
                ?: cryptoBalances.keys.firstOrNull()
            val quote = lastTradeQuoteCurrencyCode
                ?: cryptoBalances
                    .filterNot { (currencyCode, _) -> currencyCode.equals(source, true) }
                    .maxByOrNull { (_, balance) -> balance }?.key
                ?: cryptoBalances.keys.firstOrNull { !it.equals(source, true) }
            source to quote
        }
    }
}

/**
 * Create tracking event properties for the current order information.
 * Only call when [M.state] is [State.CreatingOrder] or [State.ProcessingOrder].
 */
private fun M.createOrderEventProps(error: String? = null): Map<String, String> {
    check(state is State.CreatingOrder || state is State.ProcessingOrder)
    val order = (state as? State.ProcessingOrder)?.order
    val props = mutableMapOf(
        "partner" to (order?.provider?.name ?: selectedOffer?.offer?.provider?.name).orEmpty(),
        "base_currency" to sourceCurrencyCode.orEmpty(),
        "quote_currency" to quoteCurrencyCode.orEmpty(),
        "method" to when (mode) {
            Mode.TRADE -> ExchangeInput.Media.CRYPTO.name
            Mode.BUY -> order?.inputs?.firstOrNull()?.media?.name.orEmpty()
            Mode.SELL -> order?.outputs?.firstOrNull()?.media?.name.orEmpty()
        }
    )

    error?.let { props["error"] = it }

    return props
}

private fun List<OfferDetails>.getDefaultOffer(mode: Mode): OfferDetails? {
    return maxByOrNull { details ->
        val isValidOffer = details.offer.invoiceEstimate != null
        val isCurrencyMethodReady = when (mode) {
            Mode.BUY -> details.offer.sourceCurrencyMethod.status
            Mode.SELL -> details.offer.quoteCurrencyMethod.status
            Mode.TRADE -> details.offer.sourceCurrencyMethod.status
        } == CurrencyMethod.Status.READY

        isValidOffer && isCurrencyMethodReady
    }
}

private fun List<ExchangePair>.containsBuyPairs(model: M): Boolean {
    return any { pair -> model.selectedFiatCurrency?.code == pair.fromCode }
}
