/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.os.Bundle
import android.security.keystore.UserNotAuthenticatedException
import android.text.format.DateUtils
import androidx.core.view.isVisible
import com.blockset.walletkit.Amount
import com.blockset.walletkit.TransferAttribute
import com.blockset.walletkit.TransferFeeBasis
import com.blockset.walletkit.TransferState
import com.blockset.walletkit.errors.FeeEstimationError
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.brd.api.models.ExchangeInput
import com.brd.api.models.ExchangeOrder
import com.brd.api.models.ExchangeOutput
import com.brd.exchange.ExchangeEffect
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.breadbox.BreadBox
import com.breadwallet.breadbox.TransferSpeed
import com.breadwallet.breadbox.addressFor
import com.breadwallet.breadbox.estimateFee
import com.breadwallet.breadbox.feeForSpeed
import com.breadwallet.breadbox.getSize
import com.breadwallet.breadbox.hashString
import com.breadwallet.breadbox.toBigDecimal
import com.breadwallet.databinding.ControllerTradeTransactionBinding
import com.breadwallet.logger.logError
import com.breadwallet.platform.entities.TxMetaDataValue
import com.breadwallet.platform.interfaces.AccountMetaDataProvider
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.security.isFingerPrintAvailableAndSetup
import com.breadwallet.ui.auth.AuthMode
import com.breadwallet.ui.auth.AuthenticationController
import com.breadwallet.ui.send.ConfirmTradeController
import com.breadwallet.ui.send.TransferField
import com.breadwallet.util.isHedera
import com.breadwallet.util.isRipple
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.kodein.di.instance
import java.util.Locale

class TradeTransactionController(args: Bundle? = null) :
    ExchangeController.ChildController(args),
    ConfirmTradeController.Listener,
    AuthenticationController.Listener {

    private val binding by viewBinding(ControllerTradeTransactionBinding::inflate)
    private val userManager by instance<BrdUserManager>()
    private val breadBox by instance<BreadBox>()
    private val metaDataManager by instance<AccountMetaDataProvider>()

    private var transferFeeBasis: TransferFeeBasis? = null
    private var transferAttrs: Set<TransferAttribute>? = null

    override fun handleEffect(effect: ExchangeEffect) {
        super.handleEffect(effect)
        if (effect is ExchangeEffect.ProcessUserAction) {
            val childRouter = getChildRouter(binding.container)
            if (transferFeeBasis == null) {
                val state = (currentModel.state as? ExchangeModel.State.ProcessingOrder) ?: return
                launchTrade(state, childRouter)
            }
        }
    }

    override fun onPositiveClicked() {
        val res = requireResources()
        val activity = checkNotNull(activity)
        val authenticationMode =
            if (isFingerPrintAvailableAndSetup(activity) && BRSharedPrefs.sendMoneyWithFingerprint) {
                AuthMode.USER_PREFERRED
            } else {
                AuthMode.PIN_REQUIRED
            }

        val childRouter = getChildRouter(binding.container)
        val authController = AuthenticationController(
            mode = authenticationMode,
            title = res.getString(R.string.VerifyPin_title),
            message = res.getString(R.string.VerifyPin_authorize)
        )
        childRouter.pushController(RouterTransaction.with(authController))
    }

    override fun onNegativeClicked() {
        onAuthenticationCancelled()
    }

    override fun onAuthenticationSuccess() {
        binding.layoutLoader.isVisible = true
        binding.scrim.isVisible = false

        val state = (currentModel.state as? ExchangeModel.State.ProcessingOrder) ?: return
        val input = state.order.inputs
            .filterIsInstance<ExchangeInput.CryptoTransfer>()
            .first()
        val output = state.order.outputs
            .filterIsInstance<ExchangeOutput.CryptoTransfer>()
            .firstOrNull()
        controllerScope.launch {
            val sourceCurrencyCode = checkNotNull(currentModel.sourceCurrencyCode)
            val wallet = breadBox.wallet(sourceCurrencyCode).first()
            val phrase = try {
                checkNotNull(userManager.getPhrase())
            } catch (ex: UserNotAuthenticatedException) {
                logError("Failed to get phrase.", ex)
                return@launch
            }

            val address = wallet.addressFor(input.sendToAddress) ?: return@launch
            val amount = Amount.create(input.amount.toDouble(), wallet.unit)
            val feeBasis = checkNotNull(transferFeeBasis)
            val transferAttributes = checkNotNull(transferAttrs)

            val primaryWallet = wallet.walletManager.primaryWallet
            if (primaryWallet.balance < feeBasis.fee) {
                eventConsumer.accept(
                    ExchangeEvent.OnCryptoSendActionFailed(
                        reason = ExchangeEvent.SendFailedReason.InsufficientNativeWalletBalance(
                            currencyCode = primaryWallet.currency.code,
                            requiredAmount = feeBasis.fee
                                .doubleAmount(primaryWallet.unit)
                                ?.orNull() ?: 0.0
                        )
                    )
                )
                return@launch
            }

            val newTransfer =
                wallet.createTransfer(address, amount, feeBasis, transferAttributes).orNull()
            if (newTransfer == null) {
                logError("Failed to create transfer.")
                return@launch
            }

            wallet.walletManager.submit(newTransfer, phrase)

            val transfer = breadBox.walletTransfer(wallet.currency.code, newTransfer.hashString())
                .transform { transfer ->
                    when (checkNotNull(transfer.state.type)) {
                        TransferState.Type.INCLUDED,
                        TransferState.Type.PENDING,
                        TransferState.Type.SUBMITTED -> emit(transfer)
                        TransferState.Type.DELETED,
                        TransferState.Type.FAILED -> {
                            logError("Failed to submit transfer ${transfer.state.failedError.orNull()}")
                            emit(null)
                        }
                        // Ignore pre-submit states
                        TransferState.Type.CREATED,
                        TransferState.Type.SIGNED -> Unit
                    }
                }.first()

            val result = if (transfer == null) {
                ExchangeEvent.OnCryptoSendActionFailed(
                    reason = ExchangeEvent.SendFailedReason.CreateTransferFailed
                )
            } else {
                val deviceId = BRSharedPrefs.getDeviceId()
                val size = transfer.getSize()?.toInt() ?: 0
                val fee = transfer.fee.toBigDecimal().toDouble()
                val creationTime =
                    (System.currentTimeMillis() / DateUtils.SECOND_IN_MILLIS).toInt()

                val metaData = TxMetaDataValue(
                    deviceId = deviceId,
                    comment = getTransferComment(output),
                    blockHeight = transfer.wallet.walletManager.network.height.toLong(),
                    fee = fee,
                    txSize = size,
                    creationTime = creationTime
                )
                metaDataManager.putTxMetaData(transfer, metaData)

                ExchangeEvent.OnCryptoSendActionCompleted(
                    input.actions.first(),
                    transactionHash = transfer.hashString(),
                    cancelled = false
                )
            }
            eventConsumer.accept(result)
        }
    }

    override fun onAuthenticationFailed() {
        onAuthenticationCancelled()
    }

    override fun onAuthenticationCancelled() {
        val state = (currentModel.state as? ExchangeModel.State.ProcessingOrder) ?: return
        val input = state.order.inputs
            .filterIsInstance<ExchangeInput.CryptoTransfer>()
            .first()
        eventConsumer.accept(
            ExchangeEvent.OnCryptoSendActionCompleted(
                input.actions.first(),
                transactionHash = null,
                cancelled = true
            )
        )
    }

    override fun ExchangeModel.render() {
        val state = state as? ExchangeModel.State.ProcessingOrder ?: return
        if (errorState == null) {
            state.userAction?.run {
                check(action.type == ExchangeOrder.Action.Type.CRYPTO_SEND)
                val childRouter = getChildRouter(binding.container)
                launchTrade(state, childRouter)
            }
        }
    }

    private fun launchTrade(state: ExchangeModel.State.ProcessingOrder, childRouter: Router) {
        if (childRouter.hasRootController()) return
        binding.layoutLoader.isVisible = false
        binding.scrim.isVisible = true
        viewAttachScope.launch {
            val sourceCurrencyCode = checkNotNull(currentModel.sourceCurrencyCode)
            val wallet = breadBox.wallet(sourceCurrencyCode).first()
            val input = state.order.inputs
                .filterIsInstance<ExchangeInput.CryptoTransfer>()
                .first()
            val coreTransferAttrs = wallet.transferAttributes
            val transferAttributes = transferFieldsFor(sourceCurrencyCode, input)
                .mapNotNull { field ->
                    coreTransferAttrs.find { it.key.equals(field.key, true) }
                        ?.apply { setValue(field.value) }
                }
                .toSet()
            transferAttrs = transferAttributes
            val address = wallet.addressFor(input.sendToAddress) ?: return@launch
            val amount = Amount.create(input.amount.toDouble(), wallet.unit)
            val networkFee = wallet.feeForSpeed(TransferSpeed.Priority(input.currency.code))
            val feeBasis = try {
                wallet.estimateFee(address, amount, networkFee, transferAttributes)
            } catch (e: FeeEstimationError) {
                logError("Failed to estimate fee", e)
                eventConsumer.accept(
                    ExchangeEvent.OnCryptoSendActionFailed(
                        ExchangeEvent.SendFailedReason.FeeEstimateFailed
                    )
                )
                return@launch
            }
            transferFeeBasis = feeBasis
            val transferFields = transferFieldsFor(sourceCurrencyCode, input)
            Main {
                val primaryWallet = wallet.walletManager.primaryWallet
                val transaction = RouterTransaction.with(
                    ConfirmTradeController(
                        sourceCurrencyCode,
                        input.sendToAddress,
                        TransferSpeed.Priority(sourceCurrencyCode),
                        input.amount.toBigDecimal(),
                        feeBasis.fee.doubleAmount(primaryWallet.unit).get().toBigDecimal(),
                        transferFields
                    )
                )
                childRouter.setRoot(transaction)
            }
        }
    }

    private fun transferFieldsFor(
        currencyCode: String,
        input: ExchangeInput.CryptoTransfer
    ): List<TransferField> {
        return if (input.sendToDestinationTag.isNullOrBlank()) {
            emptyList()
        } else {
            when {
                currencyCode.isRipple() -> listOf(
                    TransferField(
                        key = TransferField.DESTINATION_TAG,
                        required = true,
                        invalid = false,
                        value = input.sendToDestinationTag
                    )
                )
                currencyCode.isHedera() -> listOf(
                    TransferField(
                        key = TransferField.HEDERA_MEMO,
                        required = true,
                        invalid = false,
                        value = input.sendToDestinationTag
                    )
                )
                else -> emptyList()
            }
        }
    }

    private fun getTransferComment(output: ExchangeOutput.CryptoTransfer?): String? {
        return if (output == null) null else {
            "Sold for ${output.amount}${output.currency.code.toUpperCase(Locale.ROOT)}."
        }
    }
}
