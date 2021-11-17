/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.exchange

import com.brd.api.BrdApiClient
import com.brd.api.models.*
import com.brd.api.models.ExchangeInvoiceEstimate.FeeType.*
import com.brd.api.models.ExchangeOffer.LimitType
import com.brd.api.models.ExchangeOfferRequest.Status
import com.brd.api.models.ExchangeOrder.Action.Type.*
import com.brd.exchange.ExchangeEffect.*
import com.brd.exchange.ExchangeEvent.*
import com.brd.prefs.BrdPreferences
import com.brd.featurepromotion.*
import com.brd.util.Formatters
import com.brd.util.NumberFormatter
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.serialization.ExperimentalSerializationApi
import kt.mobius.Connection
import kt.mobius.functions.Consumer
import kotlin.native.concurrent.SharedImmutable

private const val OFFER_HTTP_POLL_MS = 1500L
private const val OFFER_HTTP_POLL_LIMIT = 8 // Timeout = POLL_MS * POLL_LIMIT
private const val OFFER_SESSION_DEBOUNCE_MS = 750L

@SharedImmutable
private val rawDecimalFormatter = Formatters.new().apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 8
    alwaysShowDecimalSeparator = false
}

interface WalletProvider {
    fun loadWalletBalances(): Map<String, Double>
    fun enableWallet(currencyId: String)
    fun receiveAddressFor(currencyId: String): String?
    fun estimateLimitMaximum(currencyId: String, targetAddress: String): Double?
    fun currencyCode(currencyId: String): String?
    fun networkCurrencyCode(currencyId: String): String?
    fun estimateFee(currencyId: String, targetAddress: String): Double?

}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class)
class ExchangeEffectHandler(
    private val output: Consumer<ExchangeEvent>,
    private val brdApi: BrdApiClient,
    private val brdPrefs: BrdPreferences,
    private val walletProvider: WalletProvider,
    private val exchangeDataLoader: ExchangeDataLoader,
    private val featurePromotionService: FeaturePromotionService,
    dispatcher: CoroutineDispatcher,
) : Connection<ExchangeEffect> {

    constructor(
        output: Consumer<ExchangeEvent>,
        brdApi: BrdApiClient,
        brdPrefs: BrdPreferences,
        walletProvider: WalletProvider,
        exchangeDataLoader: ExchangeDataLoader,
        featurePromotionService: FeaturePromotionService
    ) : this(output, brdApi, brdPrefs, walletProvider, exchangeDataLoader, featurePromotionService, Main)

    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val offerSessionFlow = MutableSharedFlow<RequestOffers>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        offerSessionFlow
            .debounce(OFFER_SESSION_DEBOUNCE_MS)
            .flatMapLatest { effect ->
                if (effect.body == null) {
                    emptyFlow()
                } else {
                    // TODO: Use WebSocket client on supported devices
                    fetchExchangeOffers(brdApi, effect.body, effect.mode, walletProvider)
                }
            }
            .onEach { output.accept(it) }
            .launchIn(scope)
    }

    override fun accept(value: ExchangeEffect) {
        when (value) {
            LoadFeaturePromotions -> scope.launch {
                output.accept(loadFeaturePromotions(featurePromotionService))
            }
            is UpdateFeaturePromotionShown -> scope.launch {
                updateFeaturePromotionsShown(value, featurePromotionService)
            }
            LoadUserPreferences -> scope.launch { output.accept(loadUserPreferences(brdApi, brdPrefs)) }
            LoadCountries -> scope.launch { output.accept(loadCountries(brdApi, exchangeDataLoader)) }
            is LoadPairs -> scope.launch { output.accept(loadPairs(value, brdApi)) }
            is RequestOffers -> scope.launch { offerSessionFlow.emit(value) }
            is UpdateRegionPreferences -> scope.launch { updateRegionPreferences(value, brdPrefs) }
            is UpdateCurrencyPreference -> scope.launch { updateCurrencyPreference(value, brdPrefs) }
            is CreateOrder ->
                scope.launch { output.accept(createOrder(value, brdApi)) }
            is ProcessBackgroundActions ->
                processBackgroundActions(value, brdApi, walletProvider)
                    .onEach { output.accept(it) }
                    .launchIn(scope)
            is UpdateLastOrderCurrency -> scope.launch { updateLastPurchaseCurrency(value, brdPrefs) }
            is UpdateLastSellCurrency -> scope.launch { updateLastSellCurrency(value, brdPrefs) }
            is UpdateLastOrderAmount -> scope.launch { updateLastOrderAmount(value, brdPrefs) }
            is LoadWalletBalances -> scope.launch { output.accept(loadWalletBalances(value, walletProvider)) }
            is LoadNativeNetworkInfo -> scope.launch { output.accept(estimateNetworkFee(value, walletProvider)) }
            is UpdateLastTradeCurrencyPair -> scope.launch { updateLastTradeCurrencyPair(value, brdPrefs) }
            is SubmitCryptoTransferHash -> scope.launch { output.accept(submitCryptoTransferHash(value, brdApi)) }
            is TrackEvent -> Unit // native
            is ProcessUserAction -> Unit // native
            ExitFlow -> Unit // native
            ErrorSignal -> Unit // native
        }
    }

    override fun dispose() {
        scope.cancel()
    }
}

private suspend fun submitCryptoTransferHash(
    effect: SubmitCryptoTransferHash,
    brdApi: BrdApiClient,
): ExchangeEvent = Default {
    val success = brdApi.submitCryptoSendTransactionId(effect.action, effect.transactionHash)
    if (success) OnCryptoSendHashUpdateSuccess else OnCryptoSendHashUpdateFailed
}

private fun loadFeaturePromotions(service: FeaturePromotionService): OnFeaturePromotionsLoaded {
    return OnFeaturePromotionsLoaded(
        service.shouldShowHydraBuy(),
        service.shouldShowHydraTrade()
    )
}

private fun updateFeaturePromotionsShown(
    effect: UpdateFeaturePromotionShown,
    service: FeaturePromotionService
) {
    if (!effect.mode.isTrade) service.markHydraBuyShown()
    if (effect.mode.isTrade) service.markHydraTradeShown()
}

private suspend fun loadUserPreferences(
    brdApi: BrdApiClient,
    brdPrefs: BrdPreferences,
): ExchangeEvent = Default {
    OnUserPreferencesLoaded(
        apiHost = brdApi.host,
        selectedCountryCode = brdPrefs.countryCode?.lowercase(),
        selectedRegionCode = brdPrefs.regionCode?.lowercase(),
        fiatCurrencyCode = brdPrefs.fiatCurrencyCode.lowercase(),
        lastPurchaseCurrencyCode = brdPrefs.lastPurchaseCurrency?.lowercase(),
        lastSellCurrencyCode = brdPrefs.lastSellCurrency?.lowercase(),
        lastTradeSourceCurrencyCode = brdPrefs.lastTradeSourceCurrency?.lowercase(),
        lastTradeQuoteCurrencyCode = brdPrefs.lastTradeQuoteCurrency?.lowercase(),
        lastOrderAmount = brdPrefs.lastOrderAmount,
    )
}

private suspend fun estimateNetworkFee(
    effect: LoadNativeNetworkInfo,
    walletProvider: WalletProvider,
): ExchangeEvent = Default {
     OnLoadedNativeNetworkInfo(
        walletProvider.currencyCode(effect.currencyId) ?: "",
        effect.currencyId,
        walletProvider.networkCurrencyCode(effect.currencyId) ?: "",
        walletProvider.estimateFee(
            effect.currencyId,
            estimateTransferTargets.getValue(effect.currencyId.split(":").first())
        ) ?: 0.0,
    )
}

private suspend fun updateRegionPreferences(
    effect: UpdateRegionPreferences,
    brdPrefs: BrdPreferences,
): Unit = Default {
    brdPrefs.countryCode = effect.countryCode
    brdPrefs.regionCode = effect.regionCode
}

private suspend fun updateCurrencyPreference(
    effect: UpdateCurrencyPreference,
    brdPrefs: BrdPreferences,
): Unit = Default {
    brdPrefs.fiatCurrencyCode = effect.currencyCode
}

private suspend fun updateLastPurchaseCurrency(
    effect: UpdateLastOrderCurrency,
    brdPrefs: BrdPreferences,
): Unit = Default {
    brdPrefs.lastPurchaseCurrency = effect.currencyCode
}

private suspend fun updateLastSellCurrency(
    effect: UpdateLastSellCurrency,
    brdPrefs: BrdPreferences,
): Unit = Default {
    brdPrefs.lastSellCurrency = effect.currencyCode
}

private suspend fun updateLastTradeCurrencyPair(
    effect: UpdateLastTradeCurrencyPair,
    brdPrefs: BrdPreferences,
): Unit = Default {
    brdPrefs.lastTradeSourceCurrency = effect.sourceCode
    brdPrefs.lastTradeQuoteCurrency = effect.quoteCode
}

private suspend fun updateLastOrderAmount(
    effect: UpdateLastOrderAmount,
    brdPrefs: BrdPreferences,
): Unit = Default {
    brdPrefs.lastOrderAmount = effect.amount
}

private suspend fun loadWalletBalances(
    effect: LoadWalletBalances,
    walletProvider: WalletProvider,
): ExchangeEvent = Default {
    val balances = walletProvider.loadWalletBalances()
    val cryptoFormatter = Formatters.crypto("")
    OnWalletBalancesLoaded(
        balances = balances,
        formattedCryptoBalances = balances.mapValues { (currencyCode, value) ->
            cryptoFormatter.currencyCode = currencyCode
            cryptoFormatter.format(value)
        },
    )
}

private suspend fun loadCountries(
    brdApi: BrdApiClient,
    exchangeDataLoader: ExchangeDataLoader,
): ExchangeEvent = Default {
    exchangeDataLoader.countries?.let { countries ->
        return@Default OnCountriesLoaded(
            countries,
            exchangeDataLoader.detectedCountryCode,
            exchangeDataLoader.detectedRegionCode,
        )
    }
    when (val result = brdApi.getExchangeCountries()) {
        is ExchangeCountriesResult.Success -> {
            OnCountriesLoaded(
                result.countries,
                result.detectedCountryCode,
                result.detectedRegionCode,
            )
        }
        is ExchangeCountriesResult.Error -> OnCountriesError(result)
    }
}

private suspend fun loadPairs(
    effect: LoadPairs,
    brdApi: BrdApiClient,
): ExchangeEvent = Default {
    val (countryCode, regionCode) = effect
    when (val result = brdApi.getExchangePairs(countryCode, regionCode, test = effect.test)) {
        is ExchangePairsResult.Success -> {
            val rates = result.supportedPairs.formatFiatRates(effect.selectedFiatCurrencyCode)
            OnPairsLoaded(result.supportedPairs, result.currencies, rates)
        }
        is ExchangePairsResult.Error -> OnPairsError(result)
    }
}

private fun fetchExchangeOffers(
    brdApi: BrdApiClient,
    originalBody: ExchangeOfferBody,
    mode: ExchangeModel.Mode,
    walletProvider: WalletProvider,
): Flow<ExchangeEvent> = flow {
    val body = if (mode.isBuy) {
        originalBody
    } else {
        val currencyId = originalBody.currencyId
        val estimateTarget = estimateTransferTargets.getValue(currencyId.split(":").first())
        val maxAmount = walletProvider.estimateLimitMaximum(currencyId, estimateTarget)
        if (maxAmount == null) {
            val error = ExchangeOfferRequestResult.Error(-1, "max estimate failed")
            emit(OnOfferRequestError(originalBody, error))
            return@flow
        }
        if (originalBody.sourceCurrencyAmount > maxAmount) {
            emit(OnOfferAmountOverridden(originalBody, maxAmount))
            originalBody.copy(sourceCurrencyAmount = maxAmount)
        } else originalBody
    }
    var request = when (val result = brdApi.createOfferRequest(body)) {
        is ExchangeOfferRequestResult.Success -> result.offerRequest
        is ExchangeOfferRequestResult.Error -> {
            emit(OnOfferRequestError(body, result))
            return@flow
        }
    }
    val sourceFormatter = when (mode) {
        ExchangeModel.Mode.BUY -> Formatters.fiat(body.sourceCurrencyCode)
        ExchangeModel.Mode.TRADE,
        ExchangeModel.Mode.SELL -> Formatters.crypto(body.sourceCurrencyCode)
    }
    val quoteFormatter = when (mode) {
        ExchangeModel.Mode.BUY,
        ExchangeModel.Mode.TRADE -> Formatters.crypto(body.quoteCurrencyCode)
        ExchangeModel.Mode.SELL -> Formatters.fiat(body.quoteCurrencyCode)
    }
    var offerDetails = request.offers.asOfferDetailsList(sourceFormatter, quoteFormatter)
    emit(OnOfferRequestUpdated(body, request, offerDetails))

    repeat(OFFER_HTTP_POLL_LIMIT) {
        if (!currentCoroutineContext().isActive || request.status == Status.COMPLETE) {
            return@flow
        }

        delay(OFFER_HTTP_POLL_MS)
        when (val updatedResult = brdApi.getOfferRequest(request.id)) {
            is ExchangeOfferRequestResult.Success -> {
                // Only emit updates when actually request is changed
                if (updatedResult.offerRequest != request) {
                    request = updatedResult.offerRequest
                    offerDetails = request.offers.asOfferDetailsList(sourceFormatter, quoteFormatter)
                    emit(OnOfferRequestUpdated(body, request, offerDetails))
                }
            }
            is ExchangeOfferRequestResult.Error -> {
                emit(OnOfferRequestError(body, updatedResult))
                return@flow
            }
        }
    }

    // Polling limit exhausted, override complete status
    if (request.status == Status.GATHERING) {
        emit(OnOfferRequestUpdated(body, request.copy(status = Status.COMPLETE), offerDetails))
    }
}

private suspend fun createOrder(
    effect: CreateOrder,
    brdApi: BrdApiClient,
): ExchangeEvent {
    return when (val result = brdApi.createOrder(effect.offer.offerId)) {
        is ExchangeOrderResult.Success -> OnOrderUpdated(result.order)
        is ExchangeOrderResult.Error -> OnOrderFailed(result.type, result.message)
    }
}

private fun processBackgroundActions(
    effect: ProcessBackgroundActions,
    brdApi: BrdApiClient,
    walletProvider: WalletProvider,
): Flow<ExchangeEvent> = flow {
    val outputActions = effect.order.outputs
        .filter { output ->
            (output as? ExchangeOutput.CryptoTransfer)?.status == ExchangeOutput.CryptoStatus.WAITING_FOR_ADDRESS
        }
        .flatMap(ExchangeOutput::actions)
        .filter { it.type == CRYPTO_REFUND_ADDRESS || it.type == CRYPTO_RECEIVE_ADDRESS }
    val inputActions = effect.order.inputs
        .filter { input ->
            (input as? ExchangeInput.CryptoTransfer)?.status == ExchangeInput.CryptoStatus.WAITING_FOR_ADDRESS
        }
        .flatMap(ExchangeInput::actions)
        .filter { it.type == CRYPTO_REFUND_ADDRESS || it.type == CRYPTO_RECEIVE_ADDRESS }
    val sellSendToActions = effect.order.inputs
        .filter { input ->
            val cryptoTransfer = input as? ExchangeInput.CryptoTransfer
            cryptoTransfer?.sendToAddress?.isEmpty() ?: false
        }
        .flatMap(ExchangeInput::actions)
        .filter { it.type == CRYPTO_SEND }

    val pendingActions = (outputActions + inputActions)
    if (pendingActions.isEmpty() && sellSendToActions.isEmpty()) {
        emit(OnOrderUpdated(effect.order))
        return@flow
    }

    pendingActions.forEach { action ->
        fun getAddress(): String? = when (action.type) {
            CRYPTO_RECEIVE_ADDRESS -> {
                val fiatOutput = effect.order.inputs.first().currency.isCrypto()
                val cryptoInput = effect.order.outputs.first().currency.isFiat()
                val currencyId = if (fiatOutput && cryptoInput) {
                    effect.order.inputs.first().currency.currencyId
                } else effect.order.outputs.first().currency.currencyId
                walletProvider.receiveAddressFor(currencyId)
            }
            CRYPTO_REFUND_ADDRESS -> {
                val currencyId = effect.order.inputs.first().currency.currencyId
                walletProvider.receiveAddressFor(currencyId)
            }
            else -> null
        }
        repeat(3) {
            val address = getAddress()
            if (!address.isNullOrBlank() && brdApi.submitCryptoAddress(action, address)) {
                return@forEach
            }
            delay(500)
        }
        emit(OnOrderFailed(ExchangeOrderResult.ErrorType.UNKNOWN_ERROR, "Failed to submit addresses"))
        return@flow
    }
    brdApi.getOrder(effect.order.orderId)?.let { updatedOrder ->
        emit(OnOrderUpdated(updatedOrder))
    }
}

private fun List<ExchangeOffer>.asOfferDetailsList(
    sourceFormatter: NumberFormatter,
    quoteFormatter: NumberFormatter,
): List<ExchangeModel.OfferDetails> = map { exchangeOffer ->
    val invoiceEstimate = exchangeOffer.invoiceEstimate
    if (invoiceEstimate == null) {
        val minSourceAmount = exchangeOffer.limits.find { limit ->
            limit.type == LimitType.SOURCE_CURRENCY_MIN
        }?.amount?.toDouble()
        val maxSourceAmount = exchangeOffer.limits.find { limit ->
            limit.type == LimitType.SOURCE_CURRENCY_MAX
        }?.amount?.toDouble()
        ExchangeModel.OfferDetails.InvalidOffer(
            offer = exchangeOffer,
            minSourceAmount = minSourceAmount,
            maxSourceAmount = maxSourceAmount,
            rawReplacementAmount = (minSourceAmount ?: maxSourceAmount)?.run(rawDecimalFormatter::format),
            formattedMinSourceAmount = minSourceAmount?.run(sourceFormatter::format),
            formattedMaxSourceAmount = maxSourceAmount?.run(sourceFormatter::format),
        )
    } else {
        val quoteSubtotal = invoiceEstimate.quoteCurrency.subtotal.toDouble()
        val sourceSubtotal = invoiceEstimate.sourceCurrency.subtotal.toDouble()
        val rate = quoteSubtotal / sourceSubtotal

        fun String.formatOrNull(formatter: NumberFormatter): String? {
            return takeUnless { isBlank() || all { it == '0' || it == '.' } }
                ?.toDouble()
                ?.run(formatter::format)
        }
        ExchangeModel.OfferDetails.ValidOffer(
            offer = exchangeOffer,
            formattedSourceRate = sourceFormatter.format(sourceSubtotal / quoteSubtotal),
            formattedSourceRatePerQuote = sourceFormatter.format(1.0) + " ≈ " + quoteFormatter.format(rate),
            formattedNetworkFee = invoiceEstimate.fees.formattedAmount(NETWORK, sourceFormatter, quoteFormatter),
            formattedPlatformFee = invoiceEstimate.fees.formattedAmount(PLATFORM, sourceFormatter, quoteFormatter),
            formattedProviderFee = invoiceEstimate.fees.formattedAmount(PROVIDER, sourceFormatter, quoteFormatter),
            formattedSourceSubtotal = sourceFormatter.format(sourceSubtotal),
            formattedSourceTotal = sourceFormatter.format(invoiceEstimate.sourceCurrency.total.toDouble()),
            formattedSourceFees = invoiceEstimate.sourceCurrency.fees.formatOrNull(sourceFormatter),
            formattedQuoteTotal = quoteFormatter.format(invoiceEstimate.quoteCurrency.total.toDouble()),
            formattedQuoteSubtotal = quoteFormatter.format(quoteSubtotal),
            formattedQuoteFees = invoiceEstimate.quoteCurrency.fees.formatOrNull(quoteFormatter),
        )
    }
}


private fun List<ExchangeInvoiceEstimate.Fee>.formattedAmount(
    type: ExchangeInvoiceEstimate.FeeType,
    sourceFormatter: NumberFormatter,
    quoteFormatter: NumberFormatter,
): String? {
    return find { it.type == type }?.run {
        val source = sourceCurrencyAmount?.toDouble()
        val quote = quoteCurrencyAmount?.toDouble()
        source?.run(sourceFormatter::format) ?: quote?.run(quoteFormatter::format)
    }
}

private fun List<ExchangePair>.formatFiatRates(
    currencyCode: String?
): Map<String, String> {
    return if (currencyCode.isNullOrBlank()) {
        emptyMap()
    } else {
        val formatter = Formatters.fiat(currencyCode)
        filter { pair -> pair.fromCode == currencyCode }
            .associateWith { pair -> formatter.format(pair.rate) }
            .mapKeys { (pair, _) -> pair.toCode.lowercase() }
    }
}

@SharedImmutable
private val estimateTransferTargets = mapOf(
    "bitcoin-mainnet" to "1AmuhVShZTywwsB9H7bKmeXBHjyMbdLQBS",
    "bitcoin-testnet" to "mwEEZ2qsuxQFWWxiKVeKSgTfC3xttHQHmX",
    "bitcoincash-mainnet" to "qph3tx0wg4xrtljkycvqw207sjfes7akr5k8zp2q4d",
    "bitcoincash-testnet" to "qph3tx0wg4xrtljkycvqw207sjfes7akr5k8zp2q4d",
    "ethereum-mainnet" to "0x2e2Ece19E57226DbEe69bcBC32059758901E3F1e",
    "ethereum-ropsten" to "0x2e2Ece19E57226DbEe69bcBC32059758901E3F1e",
    "tezos-mainnet" to "tz1S6qLbynndxDKkwUhYuficdTr2VwA2LUsV",
    "hedera-mainnet" to "0.0.293290",
    "ripple-mainnet" to "rDYKg1yYeghq8v1a9Xoy9WQzAU5cTUqdH4",
    "dogecoin-mainnet" to "DUP4XYb33EGHezJs7RYsuEqcHNhMWum2Te",
    "litecoin-mainnet" to "ltc1quku6k0dn4can94ysaas9grrpyah3eh4702q592",
)
