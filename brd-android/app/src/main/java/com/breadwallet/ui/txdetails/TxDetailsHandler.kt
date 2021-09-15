/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.txdetails

import android.content.Context
import com.breadwallet.breadbox.BreadBox
import com.breadwallet.breadbox.currencyId
import com.breadwallet.breadbox.isEthereum
import com.breadwallet.breadbox.toBigDecimal
import com.breadwallet.ext.throttleLatest
import com.breadwallet.platform.entities.TxMetaDataValue
import com.breadwallet.platform.interfaces.AccountMetaDataProvider
import com.breadwallet.repository.RatesRepository
import com.breadwallet.ui.txdetails.TxDetails.E
import com.breadwallet.ui.txdetails.TxDetails.F
import drewcarlson.mobius.flow.subtypeEffectHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformLatest
import java.math.BigDecimal

private const val MEMO_THROTTLE_MS = 500L
private const val RATE_UPDATE_MS = 60_000L

fun createTxDetailsHandler(
    context: Context,
    breadBox: BreadBox,
    metaDataProvider: AccountMetaDataProvider
) = subtypeEffectHandler<F, E> {
    addTransformer<F.UpdateMemo> { effects ->
        effects
            .throttleLatest(MEMO_THROTTLE_MS)
            .transformLatest { effect ->
                metaDataProvider.putTxMetaData(
                    breadBox.walletTransfer(effect.currencyCode, effect.transactionHash).first(),
                    TxMetaDataValue(comment = effect.memo)
                )
            }
    }

    addTransformer<F.LoadTransactionMetaData> { effects ->
        effects.flatMapLatest { effect ->
            val transaction =
                breadBox.walletTransfer(effect.currencyCode, effect.transactionHash).first()
            metaDataProvider
                .txMetaData(transaction)
                .map { E.OnMetaDataUpdated(it) }
        }
    }

    addTransformer<F.LoadFiatAmountNow> { effects ->
        val rates = RatesRepository.getInstance(context)
        effects.transformLatest { effect ->
            while (true) {
                rates.getFiatForCrypto(
                    effect.cryptoTransferredAmount,
                    effect.currencyCode,
                    effect.preferredFiatIso
                )?.let { amount ->
                    emit(E.OnFiatAmountNowUpdated(amount))
                }

                delay(RATE_UPDATE_MS)
            }
        }
    }

    addTransformer<F.LoadTransaction> { effects ->
        effects.flatMapLatest { effect ->
            breadBox.walletTransfer(effect.currencyCode, effect.transactionHash)
                .mapLatest { transfer ->
                    var gasPrice = BigDecimal.ZERO
                    var gasLimit = BigDecimal.ZERO
                    if (transfer.amount.currency.isEthereum()) {
                        val feeBasis = transfer.run {
                            confirmedFeeBasis.orNull() ?: estimatedFeeBasis.get()
                        }

                        gasPrice = feeBasis.pricePerCostFactor
                            .convert(breadBox.gweiUnit())
                            .get()
                            .toBigDecimal()

                        gasLimit = feeBasis.costFactor.toBigDecimal()
                    }
                    E.OnTransactionUpdated(
                        transfer,
                        gasPrice,
                        gasLimit,
                        breadBox.wallet(effect.currencyCode).first().currencyId
                    )
                }
        }
    }
}

private fun BreadBox.gweiUnit() =
    getSystemUnsafe()!!
        .networks
        .first { it.currency.isEthereum() }
        .run { unitsFor(currency).get() }
        .first { it.symbol.contains("gwei", true) }
