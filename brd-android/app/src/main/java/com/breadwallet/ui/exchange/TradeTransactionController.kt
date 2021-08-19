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
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.brd.api.models.ExchangeInput
import com.brd.api.models.ExchangeOrder
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.breadbox.*
import com.breadwallet.crypto.Amount
import com.breadwallet.crypto.TransferState
import com.breadwallet.crypto.errors.FeeEstimationError
import com.breadwallet.databinding.ControllerTradeTransactionBinding
import com.breadwallet.logger.logError
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.security.isFingerPrintAvailableAndSetup
import com.breadwallet.ui.auth.AuthMode
import com.breadwallet.ui.auth.AuthenticationController
import com.breadwallet.ui.changehandlers.DialogChangeHandler
import com.breadwallet.ui.send.ConfirmTradeController
import com.breadwallet.ui.send.TransferField
import com.breadwallet.util.isHedera
import com.breadwallet.util.isRipple
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import org.kodein.di.erased.instance
import java.math.BigDecimal

class TradeTransactionController(args: Bundle? = null) :
    ExchangeController.ChildController(args),
    ConfirmTradeController.Listener,
    AuthenticationController.Listener {

    private val binding by viewBinding(ControllerTradeTransactionBinding::inflate)
    private val userManager by instance<BrdUserManager>()
    private val breadBox by instance<BreadBox>()

    override fun onPositiveClicked() {
        val res = requireResources()
        val activity = checkNotNull(activity)
        val authenticationMode =
            if (isFingerPrintAvailableAndSetup(activity) && BRSharedPrefs.sendMoneyWithFingerprint) {
                AuthMode.USER_PREFERRED
            } else {
                AuthMode.PIN_REQUIRED
            }

        val authController = AuthenticationController(
            mode = authenticationMode,
            title = res.getString(R.string.VerifyPin_title),
            message = res.getString(R.string.VerifyPin_authorize)
        )
        authController.targetController = this
        router.pushController(RouterTransaction.with(authController))
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
        controllerScope.launch {
            val sourceCurrencyCode = checkNotNull(currentModel.sourceCurrencyCode)
            val wallet = breadBox.wallet(sourceCurrencyCode).first()
            val phrase = try {
                checkNotNull(userManager.getPhrase())
            } catch (ex: UserNotAuthenticatedException) {
                logError("Failed to get phrase.", ex)
                return@launch
            }

            val coreTransferAttrs = wallet.transferAttributes
            val transferAttributes = transferFieldsFor(sourceCurrencyCode, input)
                .mapNotNull { field ->
                    coreTransferAttrs.find { it.key.equals(field.key, true) }
                        ?.apply { setValue(field.value) }
                }
                .toSet()
            val address = wallet.addressFor(input.sendToAddress) ?: return@launch
            val amount = Amount.create(input.amount.toDouble(), wallet.unit)
            val networkFee = wallet.feeForSpeed(TransferSpeed.Priority(input.currency.code))
            val feeBasis = try {
                wallet.estimateFee(address, amount, networkFee, transferAttributes)
            } catch (e: FeeEstimationError) {
                logError("Failed to estimate fee", e)
                return@launch
            }

            val primaryWallet = wallet.walletManager.primaryWallet
            if (primaryWallet != wallet && primaryWallet.balance < feeBasis.fee) {
                eventConsumer.accept(
                    ExchangeEvent.OnCryptoSendActionFailed(
                        reason = ExchangeEvent.SendFailedReason.InsufficientNativeWalletBalance(
                            currencyCode = primaryWallet.currency.code,
                            requiredAmount = primaryWallet.balance.sub(feeBasis.fee)
                                ?.orNull()
                                ?.doubleAmount(primaryWallet.unit)
                                ?.orNull() ?: 0.0
                        )
                    )
                )
                return@launch
            }

            val newTransfer = wallet.createTransfer(address, amount, feeBasis, transferAttributes).orNull()
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

            if (transfer == null) {
                // TODO: handle failure
            } else {
                eventConsumer.accept(
                    ExchangeEvent.OnCryptoSendActionCompleted(
                        input.actions.first(),
                        transactionHash = transfer.hashString(),
                        cancelled = false
                    )
                )
            }
        }
    }

    override fun onAuthenticationFailed() {
        // TODO: handle failure
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
        state.userAction?.run {
            check(action.type == ExchangeOrder.Action.Type.CRYPTO_SEND)
            val childRouter = getChildRouter(binding.container)
            if (!childRouter.hasRootController()) {
                launchTrade(state, childRouter)
            }
        }
    }

    private fun launchTrade(state: ExchangeModel.State.ProcessingOrder, childRouter: Router) {
        binding.layoutLoader.isVisible = false
        binding.scrim.isVisible = true
        val input = state.order.inputs
            .filterIsInstance<ExchangeInput.CryptoTransfer>()
            .first()
        val sourceCurrencyCode = checkNotNull(currentModel.sourceCurrencyCode)
        val transferFields = transferFieldsFor(sourceCurrencyCode, input)
        val transaction = RouterTransaction.with(
            ConfirmTradeController(
                sourceCurrencyCode,
                input.sendToAddress,
                TransferSpeed.Priority(sourceCurrencyCode),
                input.amount.toBigDecimal(),
                BigDecimal.ZERO,
                transferFields
            )
        ).pushChangeHandler(DialogChangeHandler())
            .popChangeHandler(DialogChangeHandler())
        childRouter.setRoot(transaction)
    }

    private fun transferFieldsFor(currencyCode: String, input: ExchangeInput.CryptoTransfer): List<TransferField> {
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
}
