/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 12/1/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.uigift

import android.security.keystore.UserNotAuthenticatedException
import android.text.format.DateUtils
import android.util.Base64
import com.breadwallet.breadbox.BreadBox
import com.breadwallet.breadbox.addressFor
import com.breadwallet.breadbox.createExportablePaperWallet
import com.breadwallet.breadbox.defaultUnit
import com.breadwallet.breadbox.estimateFee
import com.breadwallet.breadbox.estimateMaximum
import com.breadwallet.breadbox.feeForSpeed
import com.breadwallet.breadbox.getSize
import com.breadwallet.breadbox.hashString
import com.breadwallet.breadbox.toBigDecimal
import com.blockset.walletkit.Amount
import com.blockset.walletkit.TransferState
import com.blockset.walletkit.errors.ExportablePaperWalletError
import com.blockset.walletkit.errors.FeeEstimationError
import com.blockset.walletkit.errors.FeeEstimationInsufficientFundsError
import com.blockset.walletkit.errors.LimitEstimationError
import com.blockset.walletkit.errors.LimitEstimationInsufficientFundsError
import com.blockset.walletkit.errors.TransferSubmitError
import com.breadwallet.logger.logDebug
import com.breadwallet.logger.logError
import com.breadwallet.platform.entities.GiftMetaData
import com.breadwallet.platform.entities.TxMetaDataValue
import com.breadwallet.platform.interfaces.AccountMetaDataProvider
import com.breadwallet.repository.RatesRepository
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.uigift.CreateGift.E
import com.breadwallet.ui.uigift.CreateGift.F
import com.breadwallet.ui.uigift.CreateGift.FeeEstimateError
import com.breadwallet.ui.uigift.CreateGift.MaxEstimateError
import com.breadwallet.ui.uigift.CreateGift.PaperWalletError
import drewcarlson.mobius.flow.subtypeEffectHandler
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext

private const val GIFT_BASE_URL = "https://brd.com/x/gift"

fun createCreateGiftingHandler(
    currencyId: String,
    breadBox: BreadBox,
    userManager: BrdUserManager,
    rates: RatesRepository,
    giftBackup: GiftBackup,
    metaDataManager: AccountMetaDataProvider
) = subtypeEffectHandler<F, E> {
    addFunction<F.CreatePaperWallet> {
        val wallet = breadBox.wallet(currencyId).first()
        try {
            val exportableWallet = wallet.walletManager.createExportablePaperWallet()
            val privateKey = checkNotNull(exportableWallet.key.orNull()).encodeAsPrivate()
            val address = checkNotNull(exportableWallet.address.orNull()).toString()
            val encodedPrivateKey = Base64.encode(privateKey, Base64.NO_PADDING).toString(Charsets.UTF_8)
            val url = "$GIFT_BASE_URL/$encodedPrivateKey"
            E.OnPaperWalletCreated(privateKey.toString(Charsets.UTF_8), address, url)
        } catch (e: ExportablePaperWalletError) {
            logError("Failed to create exportable paper wallet", e)
            E.OnPaperWalletFailed(PaperWalletError.UNKNOWN)
        }
    }

    addConsumer<F.BackupGift> { (address, privateKey) ->
        giftBackup.putGift(GiftCopy(address, privateKey))
    }

    addFunction<F.DeleteGiftBackup> { (address) ->
        giftBackup.removeUnusedGift(address)
        E.OnGiftBackupDeleted
    }

    addFunction<F.EstimateFee> { (address, amount, transferSpeed) ->
        val wallet = breadBox.wallet(currencyId).first()
        val coreAddress = wallet.addressFor(address)
        val networkFee = wallet.feeForSpeed(transferSpeed)
        val coreAmount = Amount.create(amount.toDouble(), wallet.unit)
        if (coreAddress == null) {
            logError("Invalid address provided")
            E.OnFeeFailed(FeeEstimateError.INVALID_TARGET)
        } else {
            try {
                E.OnFeeUpdated(wallet.estimateFee(coreAddress, coreAmount, networkFee))
            } catch (e: FeeEstimationError) {
                logError("Failed to estimate fee", e)
                when (e) {
                    is FeeEstimationInsufficientFundsError ->
                        E.OnFeeFailed(FeeEstimateError.INSUFFICIENT_BALANCE)
                    else -> E.OnFeeFailed(FeeEstimateError.SERVER_ERROR)
                }
            }
        }
    }

    addFunction<F.EstimateMax> { (address, transferSpeed, fiatCurrencyCode) ->
        val wallet = breadBox.wallet(currencyId).first()
        val coreAddress = wallet.addressFor(address)
        val networkFee = wallet.feeForSpeed(transferSpeed)
        if (coreAddress == null) {
            E.OnMaxEstimateFailed(MaxEstimateError.INVALID_TARGET)
        } else {
            try {
                var max = wallet.estimateMaximum(coreAddress, networkFee)
                if (max.unit != wallet.defaultUnit) {
                    max = checkNotNull(max.convert(wallet.defaultUnit).orNull())
                }
                val fiatPerCryptoUnit = rates.getFiatPerCryptoUnit(
                    max.currency.code,
                    fiatCurrencyCode
                )
                E.OnMaxEstimated(max.toBigDecimal() * fiatPerCryptoUnit, fiatPerCryptoUnit)
            } catch (e: LimitEstimationError) {
                logError("Failed to estimate max", e)
                when (e) {
                    is LimitEstimationInsufficientFundsError -> E.OnMaxEstimateFailed(MaxEstimateError.INSUFFICIENT_BALANCE)
                    else -> E.OnMaxEstimateFailed(MaxEstimateError.SERVER_ERROR)
                }
            }
        }
    }

    addFunction<F.SendTransaction> { (address, amount, feeBasis) ->
        val wallet = breadBox.wallet(currencyId).first()
        val coreAddress = wallet.addressFor(address)
        val coreAmount = Amount.create(amount.toDouble(), wallet.unit)
        val phrase = try {
            userManager.getPhrase()
        } catch (e: UserNotAuthenticatedException) {
            logDebug("Authentication cancelled")
            return@addFunction E.OnTransferFailed
        }
        if (coreAddress == null) {
            logError("Invalid address provided")
            E.OnTransferFailed
        } else {
            val transfer = wallet.createTransfer(coreAddress, coreAmount, feeBasis, null).orNull()
            if (transfer == null) {
                logError("Failed to create transfer")
                E.OnTransferFailed
            } else {
                try {
                    wallet.walletManager.submit(transfer, phrase)
                    val transferHash = transfer.hashString()
                    breadBox.walletTransfer(wallet.currency.code, transferHash)
                        .mapNotNull { tx ->
                            when (checkNotNull(tx.state.type)) {
                                TransferState.Type.CREATED, TransferState.Type.SIGNED -> null
                                TransferState.Type.INCLUDED, TransferState.Type.PENDING, TransferState.Type.SUBMITTED -> {
                                    EventUtils.pushEvent(EventUtils.EVENT_GIFT_SEND)
                                    E.OnTransactionSent(transferHash)
                                }
                                TransferState.Type.DELETED, TransferState.Type.FAILED -> {
                                    logError("Failed to submit transfer ${tx.state.failedError.orNull()}")
                                    E.OnTransferFailed
                                }
                            }
                        }.first()
                } catch (e: TransferSubmitError) {
                    logError("Failed to submit transfer", e)
                    E.OnTransferFailed
                }
            }
        }
    }

    addFunction<F.SaveGift> { effect ->
        withContext(NonCancellable) {
            giftBackup.markGiftIsUsed(effect.address)
            val wallet = breadBox.wallet(currencyId).first()
            val transfer = breadBox.walletTransfer(wallet.currency.code, effect.txHash).first()
            metaDataManager.putTxMetaData(
                transfer,
                TxMetaDataValue(
                    BRSharedPrefs.getDeviceId(),
                    "",
                    effect.fiatCurrencyCode,
                    effect.fiatPerCryptoUnit.toDouble(),
                    wallet.walletManager.network.height.toLong(),
                    transfer.fee.toBigDecimal().toDouble(),
                    transfer.getSize()?.toInt() ?: 0,
                    (System.currentTimeMillis() / DateUtils.SECOND_IN_MILLIS).toInt(),
                    gift = GiftMetaData(
                        keyData = effect.privateKey,
                        recipientName = effect.recipientName
                    )
                )
            )
            E.OnGiftSaved
        }
    }
}
