/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 7/26/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.wallet

import android.content.Context
import com.breadwallet.app.BreadApp
import com.breadwallet.breadbox.*
import com.blockset.walletkit.Amount
import com.blockset.walletkit.Transfer
import com.blockset.walletkit.TransferDirection
import com.breadwallet.effecthandler.metadata.MetaDataEffect
import com.breadwallet.effecthandler.metadata.MetaDataEvent
import com.breadwallet.logger.logError
import com.breadwallet.model.PriceChange
import com.breadwallet.repository.RatesRepository
import com.breadwallet.tools.manager.*
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.tools.util.TokenUtil
import com.breadwallet.ui.formatFiatForUi
import com.breadwallet.ui.models.TransactionState
import com.breadwallet.ui.wallet.WalletScreen.E
import com.breadwallet.ui.wallet.WalletScreen.F
import com.spotify.mobius.Connectable
import drewcarlson.mobius.flow.flowTransformer
import drewcarlson.mobius.flow.subtypeEffectHandler
import drewcarlson.mobius.flow.transform
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import kotlin.math.min

private const val MAX_PROGRESS = 100
private const val DELEGATE = "Delegate"

@Suppress("TooManyFunctions")
object WalletScreenHandler {

    fun createEffectHandler(
        context: Context,
        breadBox: BreadBox,
        metadataEffectHandler: Connectable<MetaDataEffect, MetaDataEvent>,
        ratesFetcher: RatesFetcher,
        connectivityStateProvider: ConnectivityStateProvider
    ) = subtypeEffectHandler<F, E> {
        addTransformer(handleLoadPricePerUnit(context))

        addTransformer(handleLoadBalance(breadBox))
        addTransformer(handleLoadTransactions(breadBox))
        addTransformer(handleLoadCurrencyName(breadBox))
        addTransformer(handleLoadSyncState(breadBox))
        addTransformer(handleWalletState(breadBox))
        addConsumer(handleCreateAccount(breadBox))

        addTransformer(handleLoadTransactionMetaData(metadataEffectHandler))
        addTransformer(handleLoadTransactionMetaDataSingle(metadataEffectHandler))
        addTransformer(handleLoadConnectivityState(connectivityStateProvider))

        addConsumerSync(Default, ::handleTrackEvent)
        addConsumerSync(Default, ::handleUpdateCryptoPreferred)
        addFunctionSync(Default, ::handleLoadIsTokenSupported)
        addFunctionSync(Default, ::handleConvertCryptoTransactions)
        addFunction(handleLoadChartInterval(ratesFetcher))
        addFunction(handleLoadMarketData(ratesFetcher))
        addFunctionSync<F.LoadCryptoPreferred>(Default) {
            E.OnIsCryptoPreferredLoaded(BRSharedPrefs.isCryptoPreferred())
        }
    }

    private fun handleUpdateCryptoPreferred(
        effect: F.UpdateCryptoPreferred
    ) {
        EventUtils.pushEvent(EventUtils.EVENT_AMOUNT_SWAP_CURRENCY)
        BRSharedPrefs.setIsCryptoPreferred(b = effect.cryptoPreferred)
    }

    private fun handleConvertCryptoTransactions(
        effect: F.ConvertCryptoTransactions
    ) = effect.transactions
        .filter { it.hash.isPresent }
        .mapNotNullOrExceptional { it.asWalletTransaction(effect.currencyId) }
        .run(E::OnTransactionsUpdated)

    private fun handleLoadIsTokenSupported(
        effect: F.LoadIsTokenSupported
    ) = TokenUtil.isTokenSupported(effect.currencyCode)
        .run(E::OnIsTokenSupportedUpdated)

    private fun handleTrackEvent(value: F.TrackEvent) {
        EventUtils.pushEvent(value.eventName, value.attributes)
    }

    private fun handleLoadPricePerUnit(
        context: Context
    ) = flowTransformer<F.LoadFiatPricePerUnit, E> { effects ->
        val ratesRepository = RatesRepository.getInstance(context)
        val fiatIso = BRSharedPrefs.getPreferredFiatIso()
        effects
            .flatMapLatest { effect ->
                ratesRepository.changes().map { effect }
            }
            .mapLatest { effect ->
                val exchangeRate =
                    ratesRepository.getFiatPerCryptoUnit(effect.currencyCode, fiatIso)
                val fiatPricePerUnit = exchangeRate.formatFiatForUi(fiatIso)
                val priceChange: PriceChange? = ratesRepository.getPriceChange(effect.currencyCode)
                E.OnFiatPricePerUpdated(fiatPricePerUnit, priceChange)
            }
    }

    private fun handleLoadChartInterval(
        ratesFetcher: RatesFetcher
    ): suspend (F.LoadChartInterval) -> E = { effect ->
        val dataPoints = ratesFetcher.getHistoricalData(
            effect.currencyCode,
            BRSharedPrefs.getPreferredFiatIso(),
            effect.interval
        )
        E.OnMarketChartDataUpdated(dataPoints)
    }

    private fun handleLoadMarketData(
        ratesFetcher: RatesFetcher
    ): suspend (F.LoadMarketData) -> E = { effect ->
        val marketDataResult = ratesFetcher.getMarketData(
            effect.currencyCode,
            BRSharedPrefs.getPreferredFiatIso()
        )
        E.OnMarketDataUpdated(marketDataResult)
    }

    private fun handleLoadTransactions(
        breadBox: BreadBox
    ) = flowTransformer<F.LoadTransactions, E> { effects ->
        effects
            .flatMapLatest { effect ->
                breadBox.walletTransfers(effect.currencyCode).combine(
                    breadBox.wallet(effect.currencyCode)
                        .mapLatest { Pair(it.walletManager.network.height, it.currencyId) }
                        .distinctUntilChangedBy { it.first }
                ) { transfers, (_, currencyId) -> Pair(transfers, currencyId) }
            }
            .mapLatest { (transactions, currencyId) ->
                E.OnTransactionsUpdated(
                    transactions
                        .filter { it.hash.isPresent }
                        .mapNotNullOrExceptional { it.asWalletTransaction(currencyId) }
                        .sortedByDescending(WalletTransaction::timeStamp)
                )
            }
    }

    private fun handleLoadBalance(breadBox: BreadBox) =
        flowTransformer<F.LoadWalletBalance, E> { effects ->
            effects
                .flatMapLatest { effect ->
                    breadBox.wallet(effect.currencyCode)
                        .map { it.balance }
                        .distinctUntilChanged()
                }
                .mapLatest { balance ->
                    E.OnBalanceUpdated(
                        balance.toBigDecimal(),
                        getBalanceInFiat(balance)
                    )
                }
        }

    private fun handleLoadCurrencyName(breadBox: BreadBox) =
        flowTransformer<F.LoadCurrencyName, E> { effects ->
            effects
                .map { effect ->
                    val wallet = breadBox.wallet(effect.currencyCode).first()
                    val name = TokenUtil.tokenForCode(effect.currencyCode)?.name
                        ?: wallet.currency.name
                    E.OnCurrencyNameUpdated(name, wallet.currency.uids)
                }
        }

    private fun handleLoadSyncState(breadBox: BreadBox) =
        flowTransformer<F.LoadSyncState, E> { effects ->
            effects
                .flatMapLatest { (currencyId) ->
                    breadBox.wallet(currencyId)
                }
                .mapLatest { wallet ->
                    E.OnSyncProgressUpdated(
                        wallet.isSyncing
                    )
                }
        }

    private fun handleLoadTransactionMetaData(
        metadataEffectHandler: Connectable<MetaDataEffect, MetaDataEvent>
    ) = flowTransformer<F.LoadTransactionMetaData, E> { effects ->
        effects
            .map { MetaDataEffect.LoadTransactionMetaData(it.currencyCode, it.transactionHashes) }
            .transform(metadataEffectHandler)
            .filterIsInstance<MetaDataEvent.OnTransactionMetaDataUpdated>()
            .map { event ->
                E.OnTransactionMetaDataUpdated(
                    event.transactionHash,
                    event.txMetaData
                )
            }
    }

    private fun handleLoadTransactionMetaDataSingle(
        metaDataEffectHandler: Connectable<MetaDataEffect, MetaDataEvent>
    ) = flowTransformer<F.LoadTransactionMetaDataSingle, E> { effects ->
        effects
            .map {
                MetaDataEffect.LoadTransactionMetaDataSingle(
                    it.currencyCode,
                    it.transactionHashes
                )
            }
            .transform(metaDataEffectHandler)
            .filterIsInstance<MetaDataEvent.OnTransactionMetaDataSingleUpdated>()
            .map { event ->
                E.OnTransactionMetaDataLoaded(event.metadata)
            }
    }

    private fun handleWalletState(breadBox: BreadBox) =
        flowTransformer<F.LoadWalletState, E> { effects ->
            effects
                .flatMapLatest {
                    breadBox.walletState(it.currencyCode)
                }
                .mapLatest {
                    E.OnWalletStateUpdated(it)
                }
        }

    private fun handleLoadConnectivityState(connectivityStateProvider: ConnectivityStateProvider) =
        flowTransformer<F.LoadConnectivityState, E> { effects ->
            effects
                .flatMapLatest {
                    connectivityStateProvider.state()
                }
                .mapLatest { state ->
                    E.OnConnectionUpdated(state == ConnectivityState.Connected)
                }
        }

    private fun handleCreateAccount(breadBox: BreadBox): suspend (F.CreateAccount) -> Unit =
        { breadBox.initializeWallet(it.currencyCode) }
}

private fun getBalanceInFiat(balanceAmt: Amount): BigDecimal {
    val context = BreadApp.getBreadContext()
    return RatesRepository.getInstance(context).getFiatForCrypto(
        balanceAmt.toBigDecimal(),
        balanceAmt.currency.code,
        BRSharedPrefs.getPreferredFiatIso()
    ) ?: BigDecimal.ZERO
}

// Note: Due to a WalletKit9 change, the wallet a Transfer is found in is no longer determinable
// from within that Transfer, thus the need for the currencyId param. If a Transfer is a fee transfer
// Transfer.wallet will resolve to the token wallet even if found in the ETH wallet
fun Transfer.asWalletTransaction(currencyId: String): WalletTransaction {
    val confirmations = confirmations.orNull()?.toInt() ?: 0
    val confirmationsUntilFinal = wallet.walletManager.network.confirmationsUntilFinal.toInt()
    val isComplete = confirmations >= confirmationsUntilFinal
    val transferState = TransactionState.valueOf(state)
    val feeForToken = feeForToken(currencyId)
    val amountInDefault = when {
        feeForToken.isNotBlank() -> fee
        amount.unit == wallet.defaultUnit -> amount
        else -> checkNotNull(amount.convert(wallet.defaultUnit).orNull())
    }
    val isErrored = state.failedError.isPresent || confirmation.orNull()?.success == false
    val delegateAddr = attributes.find { it.key.equals(DELEGATE, true) }?.value?.orNull()
    return WalletTransaction(
        txHash = hashString(),
        amount = amountInDefault.toBigDecimal(),
        amountInFiat = getBalanceInFiat(amountInDefault),
        isStaking = delegateAddr != null,
        toAddress = delegateAddr ?: target.orNull()?.toSanitizedString() ?: "<unknown>",
        fromAddress = source.orNull()?.toSanitizedString() ?: "<unknown>",
        isReceived = direction == TransferDirection.RECEIVED,
        fee = fee.doubleAmount(unitForFee.base).or(0.0).toBigDecimal(),
        confirmations = confirmations,
        isComplete = isComplete,
        isPending = when (transferState) {
            TransactionState.CONFIRMING -> true
            TransactionState.CONFIRMED -> !isComplete
            else -> false
        },
        isErrored = isErrored,
        progress = min(
            ((confirmations.toDouble() / confirmationsUntilFinal) * MAX_PROGRESS).toInt(),
            MAX_PROGRESS
        ),
        timeStamp = confirmation.orNull()?.confirmationTime?.time ?: System.currentTimeMillis(),
        currencyCode = wallet.currency.code,
        feeToken = feeForToken,
        confirmationsUntilFinal = wallet.walletManager.network.confirmationsUntilFinal.toInt()
    )
}

public inline fun <T, R : Any> Iterable<T>.mapNotNullOrExceptional(
    crossinline transform: (T) -> R?
): List<R> = mapNotNull { elem: T ->
    try {
        transform(elem)
    } catch (e: Exception) {
        logError("Exception caught, transform skipped", e)
        BRReportsManager.reportBug(e)
        null
    }
}
