/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.txdetails

import android.util.Base64
import com.breadwallet.breadbox.defaultUnit
import com.breadwallet.breadbox.feeForToken
import com.breadwallet.breadbox.isErc20
import com.breadwallet.breadbox.isEthereum
import com.breadwallet.breadbox.isReceived
import com.breadwallet.breadbox.toBigDecimal
import com.breadwallet.breadbox.toSanitizedString
import com.breadwallet.ui.models.TransactionState
import com.breadwallet.ui.send.TransferField
import com.breadwallet.ui.txdetails.TxDetails.E
import com.breadwallet.ui.txdetails.TxDetails.F
import com.breadwallet.ui.txdetails.TxDetails.M
import com.breadwallet.util.isBitcoinLike
import com.breadwallet.platform.entities.TxMetaDataEmpty
import com.breadwallet.platform.entities.TxMetaDataValue
import com.breadwallet.tools.util.GIFT_BASE_URL
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import java.math.BigDecimal
import java.util.Date

const val MAX_CRYPTO_DIGITS = 8
private const val DELEGATE = "Delegate"

object TxDetailsUpdate : Update<M, E, F>, TxDetailsUpdateSpec {

    override fun update(
        model: M,
        event: E
    ): Next<M, F> = patch(model, event)

    override fun onTransactionUpdated(
        model: M,
        event: E.OnTransactionUpdated
    ): Next<M, F> {
        val updatedModel = with(event.transaction) {
            val confirmations = confirmations.orNull()?.toInt() ?: 0
            val confirmationsUntilFinal =
                wallet.walletManager.network.confirmationsUntilFinal.toInt()
            val delegateAddr = attributes.find { it.key.equals(DELEGATE, true) }?.value?.orNull()
            val feeForToken = feeForToken(event.currencyId)
            model.copy(
                isEth = amount.currency.isEthereum(),
                isErc20 = amount.currency.isErc20(),
                cryptoTransferredAmount = if (feeForToken.isBlank()) amount.toBigDecimal(wallet.defaultUnit) else BigDecimal.ZERO,
                fee = fee.doubleAmount(wallet.unitForFee).or(0.0).toBigDecimal(),
                feeCurrency = fee.currency.code,
                isReceived = isReceived(),
                blockNumber = confirmation.orNull()?.blockNumber?.toInt() ?: 0,
                toOrFromAddress = when {
                    isReceived() && !model.currencyCode.isBitcoinLike() -> source.orNull()?.toSanitizedString()
                    delegateAddr != null -> delegateAddr
                    else -> target.orNull()?.toSanitizedString()
                } ?: "",
                confirmationDate = confirmation
                    .transform { it?.confirmationTime }
                    .or { Date() },
                confirmedInBlockNumber = confirmation
                    .transform { it?.blockNumber?.toString() }
                    .or(""),
                transactionState = TransactionState.valueOf(state),
                isCompleted = confirmations >= confirmationsUntilFinal,
                gasPrice = event.gasPrice,
                gasLimit = event.gasLimit,
                feeToken = feeForToken,
                confirmations = confirmations,
                transferFields = event.transaction
                    .attributes
                    .map { attribute ->
                        TransferField(
                            key = attribute.key,
                            required = attribute.isRequired,
                            invalid = false,
                            value = attribute.value.orNull()
                        )
                    }
            )
        }

        return next(
            updatedModel,
            setOf(
                F.LoadFiatAmountNow(
                    updatedModel.cryptoTransferredAmount,
                    updatedModel.currencyCode,
                    updatedModel.preferredFiatIso
                )
            )
        )
    }

    override fun onFiatAmountNowUpdated(
        model: M,
        event: E.OnFiatAmountNowUpdated
    ): Next<M, F> =
        next(
            model.copy(
                fiatAmountNow = event.fiatAmountNow
            )
        )

    override fun onMetaDataUpdated(
        model: M,
        event: E.OnMetaDataUpdated
    ): Next<M, F> =
        when (event.metaData) {
            is TxMetaDataValue -> {
                next(
                    model.copy(
                        memo = event.metaData.comment ?: "",
                        memoLoaded = true,
                        exchangeCurrencyCode = event.metaData.exchangeCurrency ?: "",
                        exchangeRate = event.metaData.exchangeRate.toBigDecimal(),
                        gift = event.metaData.gift
                    )
                )
            }
            is TxMetaDataEmpty -> next(
                model.copy(
                    memo = "",
                    memoLoaded = true
                )
            )
        }

    override fun onMemoChanged(
        model: M,
        event: E.OnMemoChanged
    ): Next<M, F> {
        return when {
            model.memoLoaded -> dispatch(
                setOf(
                    F.UpdateMemo(
                        model.currencyCode,
                        model.transactionHash,
                        event.memo
                    )
                )
            )
            else -> noChange()
        }
    }

    override fun onClosedClicked(model: M): Next<M, F> =
        dispatch(setOf(F.Close))

    override fun onShowHideDetailsClicked(
        model: M
    ): Next<M, F> = next(
        model.copy(
            showDetails = !model.showDetails
        )
    )

    override fun onAddressClicked(model: M): Next<M, F> =
        dispatch(setOf(F.CopyToClipboard(model.toOrFromAddress)))

    override fun onTransactionHashClicked(model: M): Next<M, F> =
        dispatch(setOf(F.CopyToClipboard(model.transactionHash)))

    override fun onGiftResendClicked(model: M): Next<M, F> {
        val gift = model.gift!!
        val key = gift.keyData!!.toByteArray()
        val encodedPrivateKey = Base64.encode(key, Base64.NO_PADDING).toString(Charsets.UTF_8)
        val giftUrl = "$GIFT_BASE_URL$encodedPrivateKey"
        return dispatch(
            setOf(
                F.ShareGift(
                    giftUrl,
                    model.transactionHash,
                    gift.recipientName!!,
                    model.cryptoTransferredAmount,
                    model.fiatAmountNow,
                    model.exchangeRate
                )
            )
        )
    }

    override fun onGiftReclaimClicked(model: M): Next<M, F> =
        dispatch(setOf(F.ImportGift(model.gift!!.keyData!!, model.transactionHash)))
}
