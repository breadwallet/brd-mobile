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
import com.brd.exchange.ExchangeModel.*
import com.brd.util.CommonLocales
import com.brd.util.Formatters
import com.brd.util.NumberFormatter
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
private val wholeNumberFormatter: NumberFormatter =
    NumberFormatter(CommonLocales.root).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }

data class ExchangeModel(
    /** The current [State]. */
    val state: State = State.Initializing,
    /** The raw user source input string, must be a valid double. */
    val sourceAmountInput: String = "",
    /** The raw user quote input string, must be a valid double or null. */
    val quoteAmountInput: String? = null,
    /** The user selected fiat crypto currency code. */
    val selectedFiatCurrency: ExchangeCurrency? = null,
    /** The user selected output currency code. */
    val quoteCurrencyCode: String? = null,
    /** The user selected input currency code. */
    val sourceCurrencyCode: String? = null,
    /** The user selected region or null if not required. */
    val selectedRegion: ExchangeRegion? = null,
    /** A list of supported currency pairs and rates. */
    val pairs: List<ExchangePair> = emptyList(),
    /** A list of available sell pairs. */
    val availableSellPairs: List<ExchangePair> = emptyList(),
    /** Formatted fiat rate strings for [selectedFiatCurrency] mapped to the crypto currency code. */
    val formattedFiatRates: Map<String, String> = emptyMap(),
    /** The user selected country or null for first time users. */
    val selectedCountry: ExchangeCountry? = null,
    /** The current offer request, see [ExchangeOfferRequest.status]. */
    val offerRequest: ExchangeOfferRequest? = null,
    /** The currently selected [ExchangeOffer] or null. */
    val selectedOffer: OfferDetails? = null,
    /** Available offers with formatter amounts, may be invalid and require input changes. */
    val offerDetails: List<OfferDetails> = emptyList(),
    /** A list of supported countries with supported [ExchangePair]s. */
    val countries: List<ExchangeCountry> = emptyList(),
    /** A map of currencies for [pairs], using the currency code as a key. */
    val currencies: Map<String, ExchangeCurrency> = emptyMap(),
    /** When true, request test offers only. */
    val test: Boolean = false,
    /** The current input selection [Mode] for input data context. */
    val mode: Mode,
    /** Enabled wallet balances mapped by currency code. */
    val cryptoBalances: Map<String, Double> = emptyMap(),
    /** Formatted strings for [cryptoBalances] */
    val formattedCryptoBalances: Map<String, String> = emptyMap(),
    /** True if [ExchangeEvent.OnWalletBalancesLoaded] recieved at least once */
    val didLoadCryptoBalances: Boolean = false,
    /** Error details if user inputs are invalid, otherwise null. */
    val inputError: InputError? = null,
    /** The last purchase currency code or "btc". */
    val lastPurchaseCurrencyCode: String = "btc",
    /** The last sell currency code or null. */
    val lastSellCurrencyCode: String? = null,
    /** The previously successful trade source currency code. */
    val lastTradeSourceCurrencyCode: String? = null,
    /** The previously successful trade quote currency code.  */
    val lastTradeQuoteCurrencyCode: String? = null,
    /** True when the user is attempting to exit during an important operation. */
    val confirmingClose: Boolean = false,
    /** The [BrdApiHost] used for this exchange session. */
    val apiHost: BrdApiHost = BrdApiHost.PRODUCTION,
    /** A previously selected [OfferDetails.InvalidOffer] used to select */
    val lastOfferSelection: OfferDetails.InvalidOffer? = null,
    /** Input amount presents eg (25%, 50%, 75%, Max) */
    val inputPresets: List<InputPreset> = emptyList(),
    /** [sourceCurrency] native network info */
    val nativeNetworkInfo: NativeNetworkInfo? = null,
    val selectedInputPreset: Int? = null,
    val errorState: ErrorState? = null,
    val settingsOnly: Boolean = false,
) {
    companion object {
        /** [ExchangeModel] factory for Swift callers. */
        fun create(mode: Mode, test: Boolean): ExchangeModel =
            ExchangeModel(mode = mode, test = test)

        fun createForSettings(): ExchangeModel =
            ExchangeModel(
                mode = Mode.BUY,
                test = false,
                settingsOnly = true,
            )
    }

    sealed class OfferDetails {
        abstract val offer: ExchangeOffer

        data class ValidOffer(
            override val offer: ExchangeOffer,
            val formattedSourceRate: String?,
            val formattedSourceRatePerQuote: String?,
            val formattedNetworkFee: String?,
            val formattedPlatformFee: String?,
            val formattedProviderFee: String?,
            val formattedSourceTotal: String,
            val formattedSourceSubtotal: String,
            val formattedSourceFees: String?,
            val formattedQuoteTotal: String,
            val formattedQuoteSubtotal: String,
            val formattedQuoteFees: String?,
        ) : OfferDetails()

        data class InvalidOffer(
            override val offer: ExchangeOffer,
            val minSourceAmount: Double?,
            val maxSourceAmount: Double?,
            val rawReplacementAmount: String?,
            val formattedMinSourceAmount: String?,
            val formattedMaxSourceAmount: String?,
        ) : OfferDetails()
    }

    data class ErrorState(
        /** An optional title generated by the error source, rely on [type] otherwise. */
        val title: String? = null,
        /** An optional message generated by the error source, rely on [type] otherwise. */
        val message: String? = null,
        /** A message intended for debugging this error state. */
        val debugMessage: String?,
        /** The type of error that occurred, used to override or decorate the [title] or [message]. */
        val type: Type,
        /** True if [ExchangeEvent.OnDialogConfirmClicked] can be dispatched to retry the failed operation. */
        val isRecoverable: Boolean,
    ) {

        sealed class Type {
            object InitializationError : Type()
            object NetworkError : Type()
            object OrderError : Type()
            object UnknownError : Type()
            data class TransactionError(
                val sendFailedReason: ExchangeEvent.SendFailedReason? = null
            ) : Type()

            object UnsupportedRegionError : Type()
            data class InsufficientNativeBalanceError(
                val currencyCode: String,
                val amount: Double,
            ) : Type()
        }
    }

    /** The [sourceAmountInput] as a double. */
    val sourceAmount: Double = sourceAmountInput.toDoubleOrNull() ?: 0.0

    /**
     * All [ExchangePair]s that represent an exchange from [sourceCurrencyCode].
     */
    val sourcePairs: List<ExchangePair> =
        if (sourceCurrencyCode == null) {
            emptyList()
        } else {
            pairs.filter { it.fromCode == sourceCurrencyCode }
        }

    /**
     * The [ExchangePair] for the selected [sourceCurrencyCode] and
     * [quoteCurrencyCode] or null if either input is missing.
     */
    val selectedPair: ExchangePair? =
        quoteCurrencyCode?.run {
            sourcePairs.find { it.toCode == quoteCurrencyCode }
        }

    /**
     * The estimated amount of [quoteCurrencyCode] to be received
     * for the given [sourceAmount].
     */
    val quoteAmount: Double = if (mode == Mode.SELL) {
        selectedPair?.inputFromOutput(sourceAmount)
    } else {
        selectedPair?.estimatedOutput(sourceAmount)
    } ?: 0.0

    /**
     * The [sourceAmount] formatted for display in the UI.
     */
    val formattedSourceAmount: String? =
        if (sourceCurrencyCode == null) {
            null
        } else {
            val currency = currencies[sourceCurrencyCode]
            if (currency?.isFiat() == true && mode == Mode.BUY) {
                Formatters.fiat(sourceCurrencyCode)
            } else {
                Formatters.crypto(sourceCurrencyCode)
            }.apply {
                minimumFractionDigits = sourceAmountInput.substringAfterLast(".", "").length
                alwaysShowDecimalSeparator = sourceAmountInput.contains(".")
            }.format(sourceAmount)
        }

    /**
     * Formatted fiat value of source input (fiatPrice * [sourceAmountInput])
     */
    val formattedSourceAmountFiatValue: String? =
        if (sourceCurrencyCode == null ||
            selectedFiatCurrency == null ||
            formattedFiatRates.isEmpty()
        ) {
            null
        } else {
            val fiatCode = selectedFiatCurrency.code
            val rate = pairs
                .find { it.fromCode == fiatCode && it.toCode == sourceCurrencyCode }
                ?.rate
            // If fiat Currency pair not found, return null, until we
            // can integrate market data interface with CoinGecko
            rate?.let {
                Formatters.fiat(fiatCode).format(sourceAmount * it).run {
                    if (mode.isTrade) "≈ $this" else this
                }
            }
        }

    /**
     * The [quoteAmount] formatted for display in the UI.
     */
    val formattedQuoteAmount: String? =
        if (quoteCurrencyCode == null) {
            null
        } else {
            val currency = currencies[quoteCurrencyCode]
            if (currency?.isFiat() == true && mode.isBuy) {
                Formatters.fiat(quoteCurrencyCode).format(quoteAmount)
            } else {
                val pairAmountString = if (currency?.isFiat() == true) {
                    Formatters.fiat(quoteCurrencyCode).format(quoteAmount)
                } else Formatters.crypto(quoteCurrencyCode).format(quoteAmount)
                val amountString = quoteAmountInput
                    ?: (selectedOffer as? OfferDetails.ValidOffer)?.formattedQuoteTotal
                    ?: pairAmountString
                amountString.let { output -> "≈ $output" }
            }
        }

    /**
     * The current offer state given the user input or ongoing network actions.
     */
    val offerState: OfferState = when {
        sourceAmount == 0.0 || inputError != null -> OfferState.IDLE
        offerRequest == null -> OfferState.GATHERING
        offerRequest.status == ExchangeOfferRequest.Status.GATHERING -> OfferState.GATHERING
        offerRequest.offers.isEmpty() -> OfferState.NO_OFFERS
        else -> OfferState.COMPLETED
    }

    /**
     * True when the user has available sell pairs.
     */
    val hasSellPairs: Boolean
        get() = pairs.any { currencies[it.toCode]?.isFiat() == true }

    /**
     * True when the users has available assets to trade or sell.
     */
    val hasWalletBalances: Boolean
        get() = cryptoBalances.any { (_, balance) -> balance > 0 }

    /**
     * Returns a new [ExchangeOfferBody] if all user inputs are
     * available, otherwise null.
     */
    fun offerBodyOrNull(): ExchangeOfferBody? {
        return if (
            sourceCurrencyCode == null ||
            quoteCurrencyCode == null ||
            selectedCountry == null ||
            (selectedCountry.regions.isNotEmpty() && selectedRegion == null) ||
            sourceAmount == 0.0
        ) {
            null
        } else {
            ExchangeOfferBody(
                countryCode = selectedCountry.code,
                regionCode = selectedRegion?.code,
                sourceCurrencyCode = sourceCurrencyCode,
                quoteCurrencyCode = quoteCurrencyCode,
                sourceCurrencyAmount = sourceAmount,
                currencyId = checkNotNull(currencies[sourceCurrencyCode]).currencyId,
                test = test,
            )
        }
    }

    /**
     * True if the details in [body] match the corresponding
     * [ExchangeModel] properties.
     */
    fun matchesOfferBody(body: ExchangeOfferBody): Boolean {
        return sourceCurrencyCode == body.sourceCurrencyCode &&
            quoteCurrencyCode == body.quoteCurrencyCode &&
            sourceAmount == body.sourceCurrencyAmount &&
            selectedCountry?.code == body.countryCode &&
            selectedRegion?.code == body.regionCode
    }

    /**
     * true when the user's settings are fully configured and it is safe
     * for [state] to become [State.OrderSetup].
     */
    fun isRegionConfigured(): Boolean {
        val fiatCurrencies = currencies.values.filter(ExchangeCurrency::isFiat)
            .ifEmpty { countries.map(ExchangeCountry::currency).distinct() }
        return selectedCountry != null &&
            (selectedCountry.regions.isEmpty() || selectedRegion != null) &&
            (selectedFiatCurrency != null && fiatCurrencies.any { it.code == selectedFiatCurrency.code })
    }

    /**
     * Create an event tag appending .[action] to the current mode.
     */
    fun event(action: String): String =
        "${mode.name.lowercase()}.$action"

    enum class Mode {
        /**
         * In [BUY] mode, the [sourceCurrencyCode] will always be a supported
         * fiat currency and [quoteCurrencyCode] a crypto currency.
         */
        BUY,

        /**
         * In [SELL] mode, the [quoteCurrencyCode] will always be a supported
         * crypto currency and [sourceCurrencyCode] a fiat currency.
         */
        SELL,

        /**
         * In [TRADE] mode, the [sourceCurrencyCode] and [quoteCurrencyCode]
         * will always be supported crypto currencies.
         */
        TRADE;

        /**
         * Returns true when [currency] is a valid source currency for the mode.
         */
        fun isCompatibleSource(currency: ExchangeCurrency): Boolean =
            when (this) {
                TRADE, SELL -> currency.isCrypto()
                BUY -> currency.isFiat()
            }

        /**
         * Returns true when [currency] is a valid quote currency for the mode.
         */
        fun isCompatibleQuote(currency: ExchangeCurrency): Boolean =
            when (this) {
                TRADE, BUY -> currency.isCrypto()
                SELL -> currency.isFiat()
            }

        val isTrade: Boolean
            get() = this == TRADE

        val isBuy: Boolean
            get() = this == BUY

        val isSell: Boolean
            get() = this == SELL
    }

    enum class OfferState {
        /**
         * Not enough user input to request offers.
         */
        IDLE,

        /**
         * Attempting to collect offers and make a default selection.
         */
        GATHERING,

        /**
         * No offers available for the selected pairs and amount.
         */
        NO_OFFERS,

        /**
         * Successfully collected available offers.
         *
         * @see ExchangeModel.offerDetails
         * @see ExchangeModel.offerRequest
         * @see ExchangeModel.selectedOffer
         */
        COMPLETED,
    }

    sealed class State {
        /** Showing feature promotion */
        object FeaturePromotion : State()

        /** Loading exchange and region preferences. */
        object Initializing : State()

        /** Configuring location and fiat options. */
        data class ConfigureSettings(
            val target: ConfigTarget,
            val isNewUser: Boolean,
            val fiatCurrencies: List<ExchangeCurrency>,
        ) : State() {
            override fun toString(): String {
                return "ConfigureSettings(" +
                    "target=$target, " +
                    "isNewUser=$isNewUser, " +
                    "fiatCurrencies=(size:${fiatCurrencies.size})" +
                    ")"
            }
        }

        /** All the wallets have zero balances(TRADE), or user has no valid pairs for SELL */
        data class EmptyWallets(
            val sellingUnavailable: Boolean = false,
            val invalidSellPairs: Boolean = false
        ) : State()

        /** Choosing the source or quote currency depending on [source]. */
        data class SelectAsset(
            val assets: List<ExchangeCurrency>,
            val source: Boolean,
        ) : State() {
            override fun toString(): String {
                return "SelectAsset(" +
                    "assets=(size:${assets.size}), " +
                    "source=$source" +
                    ")"
            }
        }

        /** Entering source amount end selecting offer. */
        data class OrderSetup(
            val selectingOffer: Boolean = false,
        ) : State()

        /** Confirming offer details and waiting for order requirements. */
        data class CreatingOrder(
            val previewing: Boolean,
        ) : State()

        /** Processing background and user assisted order actions. */
        data class ProcessingOrder(
            val order: ExchangeOrder,
            val offerDetails: OfferDetails.ValidOffer,
            val userAction: ExchangeEffect.ProcessUserAction? = null,
        ) : State()

        /** All actions are complete, order may or may not be fully processed.  */
        data class OrderComplete(
            val order: ExchangeOrder,
            val offerDetails: OfferDetails.ValidOffer,
        ) : State()
    }

    /** An option to be configured during [State.ConfigureSettings]. */
    enum class ConfigTarget {
        /**
         * Display navigation to other [ConfigTarget]s.
         *
         * @see ExchangeEvent.OnConfigureCountryClicked
         * @see ExchangeEvent.OnConfigureRegionClicked
         * @see ExchangeEvent.OnConfigureSettingsClicked
         * @see ExchangeEvent.OnConfigureCurrencyClicked
         */
        MENU,

        /**
         *  Display user country selection.
         *
         *  @see ExchangeModel.countries
         *  @see ExchangeModel.selectedCountry
         */
        COUNTRY,

        /**
         * Display user region selection if required.
         *
         * @see ExchangeModel.selectedCountry
         * @see ExchangeModel.selectedRegion
         */
        REGION,

        /**
         * Display user fiat currency selection.
         *
         * @see ExchangeModel.State.ConfigureSettings.fiatCurrencies
         * @see ExchangeModel.selectedFiatCurrency
         */
        CURRENCY,
    }

    /**
     * Represents an InputError.
     */
    sealed class InputError {
        data class BalanceLow(
            val balance: Double,
        ) : InputError()

        data class InsufficientNativeCurrencyBalance(
            val currencyCode: String,
            val fee: Double,
        ) : InputError()
    }

    /**
     * Represents info about network native currency
     */
    data class NativeNetworkInfo(
        val currencyCode: String,
        val currencyId: String,
        val networkCurrencyCode: String,
        val feeAmount: Double
    )

    /**
     * Represents an InputPreset.
     */
    class InputPreset(
        amount: Double,
        currencyCode: String,
        percentage: Double? = null,
        type: Type
    ) {

        enum class Type {
            AMOUNT,
            PERCENTAGE
        }

        val formattedAmount: String =
            if (type == Type.PERCENTAGE) {
                val formatter = wholeNumberFormatter
                val pctVal = (percentage ?: 0.0) * 100.0
                if (percentage == 1.0) "Max" else "${formatter.format(pctVal)}%"
            } else {
                val formatter = Formatters.fiat(currencyCode)
                formatter.maximumFractionDigits = 0
                formatter.format(amount)
            }

        val amountString: String = if (type == Type.PERCENTAGE) {
            val formatter = Formatters.crypto(currencyCode)
            formatter.currencyCode = ""
            formatter.format(amount)
        } else {
            wholeNumberFormatter.format(amount)
        }

        companion object {

            private const val digitNum: Double = 100000.0
            private val buyPresetSupportedCurrencies: List<String> = listOf(
                "usd", "eur", "gbp", "cad", "aud", "cfh", "nzd", "sgd", "bnd"
            )

            fun createPercentage(
                percentage: Double,
                currencyCode: String,
                total: Double
            ): InputPreset = InputPreset(
                amount = kotlin.math.round(total * percentage * digitNum) / digitNum,
                currencyCode = currencyCode,
                percentage = percentage,
                type = Type.PERCENTAGE
            )

            fun createAmount(amount: Double, currencyCode: String): InputPreset =
                InputPreset(
                    amount = amount,
                    currencyCode = currencyCode,
                    type = Type.AMOUNT
                )

            fun defaultPctPresets(total: Double, currencyCode: String): List<InputPreset> =
                listOf(0.25, 0.5, 0.75, 1.0).map {
                    createPercentage(it, currencyCode, total)
                }

            fun defaultPresets(currencyCode: String): List<InputPreset> {
                if (!buyPresetSupportedCurrencies.contains(currencyCode)) {
                    return emptyList()
                }
                return listOf(100.0, 250.0, 500.0, 1000.0).map {
                    createAmount(it, currencyCode)
                }
            }
        }
    }

    override fun toString(): String {
        return "ExchangeModel(" +
            "state=$state, " +
            "sourceAmountInput='$sourceAmountInput', " +
            "quoteAmountInput=$quoteAmountInput, " +
            "selectedFiatCurrency=$selectedFiatCurrency, " +
            "quoteCurrencyCode=$quoteCurrencyCode, " +
            "sourceCurrencyCode=$sourceCurrencyCode, " +
            "selectedRegion=${selectedRegion?.name}, " +
            "pairs=(size:${pairs.size}), " +
            "availableSellPairs=(size:${availableSellPairs.size}), " +
            "formattedFiatRates=(size:${formattedFiatRates.size}), " +
            "selectedCountry=$selectedCountry, " +
            "offerRequest=${offerRequest?.status}, " +
            "selectedOffer=${selectedOffer?.offer?.provider?.name}, " +
            "offerDetails=(${offerDetails.joinToString { it.offer.provider.name }}), " +
            "countries=(size:${countries.size}), " +
            "currencies=(size:${currencies.size}), " +
            "test=$test, " +
            "mode=$mode, " +
            "cryptoBalances=(size:${cryptoBalances.size}), " +
            "didLoadCryptoBalances=(${didLoadCryptoBalances}), " +
            "formattedCryptoBalances=(size:${formattedCryptoBalances.size}), " +
            "inputError=$inputError, " +
            "lastPurchaseCurrencyCode='$lastPurchaseCurrencyCode', " +
            "lastTradeSourceCurrencyCode=$lastTradeSourceCurrencyCode, " +
            "lastTradeQuoteCurrencyCode=$lastTradeQuoteCurrencyCode, " +
            "confirmingClose=$confirmingClose, " +
            "apiHost=$apiHost, " +
            "lastOfferSelection=$lastOfferSelection, " +
            "errorState=$errorState, " +
            "settingsOnly=$settingsOnly, " +
            "sourceAmount=$sourceAmount, " +
            "sourcePairs=(size:${sourcePairs.size}), " +
            "selectedPair=$selectedPair, " +
            "quoteAmount=$quoteAmount, " +
            "formattedSourceAmount=$formattedSourceAmount, " +
            "formattedSourceAmountFiatValue=$formattedSourceAmountFiatValue, " +
            "formattedQuoteAmount=$formattedQuoteAmount, " +
            "offerState=$offerState" +
            ")"
    }
}
