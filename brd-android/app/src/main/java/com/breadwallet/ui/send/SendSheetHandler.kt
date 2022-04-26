/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/13/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.send

import android.content.Context
import android.security.keystore.UserNotAuthenticatedException
import com.brd.addressresolver.AddressResolver
import com.brd.addressresolver.AddressResult
import com.brd.addressresolver.AddressType
import com.breadwallet.R
import com.breadwallet.breadbox.BreadBox
import com.breadwallet.breadbox.addressFor
import com.breadwallet.breadbox.defaultUnit
import com.breadwallet.breadbox.estimateFee
import com.breadwallet.breadbox.estimateMaximum
import com.breadwallet.breadbox.feeForSpeed
import com.breadwallet.breadbox.hashString
import com.breadwallet.breadbox.toBigDecimal
import com.blockset.walletkit.Address
import com.blockset.walletkit.Amount
import com.blockset.walletkit.Transfer
import com.blockset.walletkit.TransferState
import com.blockset.walletkit.errors.FeeEstimationError
import com.blockset.walletkit.errors.LimitEstimationError
import com.breadwallet.effecthandler.metadata.MetaDataEffect
import com.breadwallet.effecthandler.metadata.MetaDataEvent
import com.breadwallet.ext.isZero
import com.breadwallet.logger.logError
import com.breadwallet.logger.logWarning
import com.breadwallet.repository.RatesRepository
import com.breadwallet.tools.manager.BRClipboardManager
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.BrdUserManager
import com.breadwallet.tools.security.isFingerPrintAvailableAndSetup
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.tools.util.Link
import com.breadwallet.tools.util.asLink
import com.breadwallet.ui.send.SendSheet.E
import com.breadwallet.ui.send.SendSheet.F
import com.breadwallet.util.*
import com.platform.APIClient
import com.spotify.mobius.Connectable
import drewcarlson.mobius.flow.flowTransformer
import drewcarlson.mobius.flow.subtypeEffectHandler
import drewcarlson.mobius.flow.transform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal

private const val RATE_UPDATE_MS = 60_000L

object SendSheetHandler {

    @Suppress("LongParameterList")
    fun create(
        context: Context,
        breadBox: BreadBox,
        uriParser: CryptoUriParser,
        userManager: BrdUserManager,
        apiClient: APIClient,
        ratesRepository: RatesRepository,
        metaDataEffectHandler: Connectable<MetaDataEffect, MetaDataEvent>,
        addressResolver: AddressResolver
    ) = subtypeEffectHandler<F, E> {
        addTransformer(pollExchangeRate(breadBox, ratesRepository))
        addTransformer(handleLoadBalance(breadBox, ratesRepository))
        addTransformer(validateAddress(breadBox, uriParser, addressResolver))
        addTransformer(handleEstimateFee(breadBox))
        addTransformer(handleEstimateMax(breadBox))
        addTransformer(handleSendTransaction(breadBox, userManager))
        addTransformer(handleAddTransactionMetadata(metaDataEffectHandler))
        addTransformer(handleLoadCryptoRequestData(breadBox, apiClient, context))
        addTransformer(handleContinueWithPayment(userManager, breadBox))
        addTransformer(handlePostPayment(apiClient))
        addFunction(handleResolveAddress(breadBox, addressResolver, uriParser))
        addFunction(parseClipboard(context, breadBox, uriParser, addressResolver))
        addFunction(handleGetTransferFields(breadBox))
        addFunction(handleValidateTransferFields(breadBox))
        addConsumer<F.TrackEvent> { (event, attrs) ->
            EventUtils.pushEvent(event, attrs)
        }

        addFunctionSync<F.LoadAuthenticationSettings> {
            val isEnabled =
                isFingerPrintAvailableAndSetup(context) && BRSharedPrefs.sendMoneyWithFingerprint
            E.OnAuthenticationSettingsUpdated(isEnabled)
        }
    }

    private fun handleGetTransferFields(
        breadBox: BreadBox
    ): suspend (F.GetTransferFields) -> E = { (currencyCode, targetAddress) ->
        val wallet = breadBox.wallet(currencyCode).first()
        val network = wallet.walletManager.network
        val address = Address.create(targetAddress, network).orNull()
        val fields = when (address) {
            null -> wallet.transferAttributes
            else -> wallet.getTransferAttributesFor(address)
        }.map { attribute ->
            TransferField(
                attribute.key,
                attribute.isRequired,
                false,
                attribute.value.orNull()
            )
        }
        E.OnTransferFieldsUpdated(fields)
    }

    private fun handleValidateTransferFields(
        breadBox: BreadBox
    ): suspend (F.ValidateTransferFields) -> E = { effect ->
        val (currencyCode, targetAddress, transferFields) = effect

        val wallet = breadBox.wallet(currencyCode).first()
        val network = wallet.walletManager.network
        val address = Address.create(targetAddress, network).orNull()

        val validatedFields = when (address) {
            null -> wallet.transferAttributes
            else -> wallet.getTransferAttributesFor(address)
        }.mapNotNull { attribute ->
            val field = transferFields.find { it.key == attribute.key }
            if (field != null) {
                attribute.setValue(field.value)
                field.copy(
                    invalid = wallet.validateTransferAttribute(attribute).isPresent
                )
            } else null
        }

        E.OnTransferFieldsUpdated(validatedFields)
    }

    private fun handleLoadBalance(
        breadBox: BreadBox,
        rates: RatesRepository
    ) = flowTransformer<F.LoadBalance, E> { effects ->
        effects.map { effect ->
            val wallet = breadBox.wallet(effect.currencyCode).first()
            val feeCurrencyCode = wallet.unitForFee.currency.code
            val balanceMin = wallet.balanceMinimum.orNull()?.toBigDecimal() ?: BigDecimal.ZERO
            val balanceBig =
                (wallet.balance.toBigDecimal() - balanceMin).coerceAtLeast(BigDecimal.ZERO)
            val fiatBig = getBalanceInFiat(balanceBig, wallet.balance, rates)
            val feeCurrencyBalance = if (effect.currencyCode.equals(feeCurrencyCode, true)) {
                balanceBig
            } else {
                val feeWallet = breadBox.wallet(feeCurrencyCode).first()
                val feeBalanceMin =
                    feeWallet.balanceMinimum.orNull()?.toBigDecimal() ?: BigDecimal.ZERO
                (feeWallet.balance.toBigDecimal() - feeBalanceMin).coerceAtLeast(BigDecimal.ZERO)
            }
            E.OnBalanceUpdated(balanceBig, fiatBig, feeCurrencyCode, feeCurrencyBalance)
        }
    }

    private fun handleEstimateMax(
        breadBox: BreadBox
    ) = flowTransformer<F.EstimateMax, E> { effects ->
        effects.mapNotNull { effect ->
            val wallet = breadBox.wallet(effect.currencyCode).first()

            // Skip if address is not valid
            val address = wallet.addressFor(effect.address) ?: return@mapNotNull null
            if (wallet.containsAddress(address))
                return@mapNotNull null

            val networkFee = wallet.feeForSpeed(effect.transferSpeed)
            try {
                var amount = wallet.estimateMaximum(address, networkFee)
                if (amount.unit != wallet.defaultUnit) {
                    amount = checkNotNull(amount.convert(wallet.defaultUnit).orNull())
                }
                E.OnMaxEstimated(amount.toBigDecimal())
            } catch (e: LimitEstimationError) {
                E.OnMaxEstimateFailed
            } catch (e: IllegalStateException) {
                E.OnMaxEstimateFailed
            }
        }
    }

    private fun handleEstimateFee(
        breadBox: BreadBox
    ) = flowTransformer<F.EstimateFee, E> { effects ->
        effects.mapNotNull { effect ->
            val wallet = breadBox.wallet(effect.currencyCode).first()

            // Skip if address is not valid
            val address = wallet.addressFor(effect.address) ?: return@mapNotNull null
            if (wallet.containsAddress(address))
                return@mapNotNull null

            val amount = Amount.create(effect.amount.toDouble(), wallet.unit)
            val networkFee = wallet.feeForSpeed(effect.transferSpeed)

            try {
                val data = wallet.estimateFee(address, amount, networkFee)
                val fee = data.fee.toBigDecimal()
                check(!fee.isZero()) { "Estimated fee was zero" }
                E.OnNetworkFeeUpdated(effect.address, effect.amount, fee, data)
            } catch (e: FeeEstimationError) {
                logError("Failed get fee estimate", e)
                E.OnNetworkFeeError
            } catch (e: IllegalStateException) {
                logError("Failed get fee estimate", e)
                E.OnNetworkFeeError
            }
        }
    }

    private fun pollExchangeRate(
        breadBox: BreadBox,
        rates: RatesRepository
    ) = flowTransformer<F.LoadExchangeRate, E> { effects ->
        effects.transformLatest { effect ->
            val wallet = breadBox.wallet(effect.currencyCode).first()
            val feeCurrencyCode = wallet.unitForFee.currency.code
            while (true) {
                val fiatRate =
                    rates.getFiatForCrypto(BigDecimal.ONE, effect.currencyCode, effect.fiatCode)
                val fiatFeeRate = when {
                    !effect.currencyCode.equals(feeCurrencyCode, false) ->
                        rates.getFiatForCrypto(BigDecimal.ONE, feeCurrencyCode, effect.fiatCode)
                    else -> fiatRate
                }

                emit(
                    E.OnExchangeRateUpdated(
                        fiatPricePerUnit = fiatRate ?: BigDecimal.ZERO,
                        fiatPricePerFeeUnit = fiatFeeRate ?: BigDecimal.ZERO,
                        feeCurrencyCode = feeCurrencyCode
                    )
                )

                // TODO: Display out of date, invalid (0) rate, etc.
                delay(RATE_UPDATE_MS)
            }
        }
    }

    private fun validateAddress(
        breadBox: BreadBox,
        uriParser: CryptoUriParser,
        addressResolver: AddressResolver
    ) = flowTransformer<F.ValidateAddress, E> { effects ->
        effects.mapLatest { effect ->
            validateTargetString(
                breadBox,
                addressResolver,
                uriParser,
                effect.currencyCode,
                effect.address
            )
        }
    }

    private fun handleResolveAddress(
        breadBox: BreadBox,
        addressResolver: AddressResolver,
        uriParser: CryptoUriParser
    ): suspend (F.ResolveAddress) -> E = { effect ->
        val nativeCurrency = breadBox.wallet(effect.currencyCode).first().walletManager.currency.code
        val result = addressResolver.resolveAddress(effect.type, effect.address, effect.currencyCode, nativeCurrency)
        when (result) {
            AddressResult.NoAddress -> E.OnAddressValidated.NoAddress(effect.type)
            AddressResult.Invalid -> E.OnAddressValidated.InvalidAddress(effect.type)
            AddressResult.ExternalError -> E.OnAddressValidated.ResolveError(effect.type)

            is AddressResult.Success -> when (
                val validateResult =
                    validateTargetString(breadBox, addressResolver, uriParser, effect.currencyCode, result.address)
            ) {
                is E.OnAddressValidated.ValidAddress -> E.OnAddressValidated.ValidAddress(
                    effect.type,
                    validateResult.address,
                    effect.address,
                    result.destinationTag
                )
                is E.OnAddressValidated.NoAddress -> E.OnAddressValidated.NoAddress(effect.type)
                else -> E.OnAddressValidated.InvalidAddress(effect.type)
            }
        }
    }

    private fun parseClipboard(
        context: Context,
        breadBox: BreadBox,
        uriParser: CryptoUriParser,
        addressResolver: AddressResolver
    ): suspend (F.ParseClipboardData) -> E = { effect ->
        val text = withContext(Dispatchers.Main) {
            BRClipboardManager.getClipboard()
        }
        validateTargetString(breadBox, addressResolver, uriParser, effect.currencyCode, text, true)
    }

    private suspend fun validateTargetString(
        breadBox: BreadBox,
        addressResolver: AddressResolver,
        uriParser: CryptoUriParser,
        currencyCode: CurrencyCode,
        target: String,
        fromClipboard: Boolean = false
    ): E.OnAddressValidated {
        val cryptoRequest = (target.asLink(breadBox, uriParser) as? Link.CryptoRequestUrl)
        val reqAddress = cryptoRequest?.address ?: target

        return when (val type = addressResolver.getAddressType(target)) {
            null -> E.OnAddressValidated.NoAddress(AddressType.NativePublic, fromClipboard)
            AddressType.NativePublic -> {
                val wallet = breadBox.wallet(currencyCode).first()
                val address = wallet.addressFor(reqAddress)
                if (address == null || wallet.containsAddress(address)) {
                    E.OnAddressValidated.InvalidAddress(AddressType.NativePublic, fromClipboard)
                } else {
                    E.OnAddressValidated.ValidAddress(AddressType.NativePublic, reqAddress)
                }
            }
            else -> E.OnAddressValidated.ResolvableAddress(type as AddressType.Resolvable, target)
        }
    }

    private fun handleSendTransaction(
        breadBox: BreadBox,
        userManager: BrdUserManager
    ) = flowTransformer<F.SendTransaction, E> { effects ->
        effects.mapLatest { effect ->
            val wallet = breadBox.wallet(effect.currencyCode).first()
            val address = wallet.addressFor(effect.address)
            val amount = Amount.create(effect.amount.toDouble(), wallet.unit)
            val feeBasis = effect.transferFeeBasis
            val fields = effect.transferFields

            if (address == null || wallet.containsAddress(address)) {
                return@mapLatest E.OnAddressValidated.InvalidAddress(AddressType.NativePublic)
            }

            val attributes = wallet.getTransferAttributesFor(address)
            attributes.forEach { attribute ->
                fields.find { it.key == attribute.key }
                    ?.let { field ->
                        attribute.setValue(field.value)
                    }
            }

            if (attributes.any { wallet.validateTransferAttribute(it).isPresent }) {
                return@mapLatest E.OnSendFailed
            }

            val phrase = try {
                checkNotNull(userManager.getPhrase())
            } catch (e: UserNotAuthenticatedException) {
                logError("Failed to get phrase.", e)
                return@mapLatest E.OnSendFailed
            }

            val newTransfer =
                wallet.createTransfer(address, amount, feeBasis, attributes).orNull()

            if (newTransfer == null) {
                logError("Failed to create transfer.")
                E.OnSendFailed
            } else {
                wallet.walletManager.submit(newTransfer, phrase)
                breadBox.walletTransfer(effect.currencyCode, newTransfer)
                    .mapToSendEvent()
                    .first()
            }
        }
    }

    private fun handleAddTransactionMetadata(
        metaDataEffectHandler: Connectable<MetaDataEffect, MetaDataEvent>
    ) = flowTransformer<F.AddTransactionMetaData, E> { effects ->
        effects
            .map { effect ->
                MetaDataEffect.AddTransactionMetaData(
                    effect.transaction,
                    effect.memo,
                    effect.fiatCurrencyCode,
                    effect.fiatPricePerUnit
                )
            }
            .transform(metaDataEffectHandler)
            .transform { } // Ignore output
    }

    private fun getBalanceInFiat(
        balanceBig: BigDecimal,
        balanceAmt: Amount,
        rates: RatesRepository
    ) = rates.getFiatForCrypto(
        balanceBig,
        balanceAmt.currency.code,
        BRSharedPrefs.getPreferredFiatIso()
    ) ?: BigDecimal.ZERO

    private fun handleLoadCryptoRequestData(
        breadBox: BreadBox,
        apiClient: APIClient,
        context: Context
    ) = flowTransformer<F.PaymentProtocol.LoadPaymentData, E.PaymentProtocol> { effects ->
        effects.map { effect ->
            val acceptHeader = effect.cryptoRequestUrl.currencyCode.getPaymentRequestHeader()
            val request: Request =
                Request.Builder().url(effect.cryptoRequestUrl.rUrlParam.orEmpty()).get()
                    .addHeader(BRConstants.HEADER_ACCEPT, acceptHeader)
                    .build()
            val response = apiClient.sendRequest(request, false)

            if (response.isSuccessful) {
                val wallet = breadBox.wallet(effect.cryptoRequestUrl.currencyCode).first()
                val paymentProtocolRequest = buildPaymentProtocolRequest(wallet, response)
                if (paymentProtocolRequest != null) {
                    E.PaymentProtocol.OnPaymentLoaded(
                        paymentProtocolRequest,
                        paymentProtocolRequest.totalAmount.get().convert(wallet.unit).get()
                            .toBigDecimal()
                    )
                } else {
                    E.PaymentProtocol.OnLoadFailed(
                        context.getString(R.string.PaymentProtocol_Errors_badPaymentRequest)
                    )
                }
            } else {
                E.PaymentProtocol.OnLoadFailed(context.getString(R.string.Send_remoteRequestError))
            }
        }
    }

    private fun handleContinueWithPayment(
        userManager: BrdUserManager,
        breadBox: BreadBox
    ) = flowTransformer<F.PaymentProtocol.ContinueWitPayment, E> { effects ->
        effects.mapLatest { effect ->
            val paymentRequest = effect.paymentProtocolRequest
            val transfer = paymentRequest.createTransfer(effect.transferFeeBasis).orNull()
            checkNotNull(transfer) { "Failed to create transfer." }

            val phrase = try {
                checkNotNull(userManager.getPhrase())
            } catch (e: UserNotAuthenticatedException) {
                logError("Failed to get phrase.", e)
                return@mapLatest E.OnSendFailed
            }

            check(paymentRequest.signTransfer(transfer, phrase)) {
                "Failed to sign transfer"
            }

            paymentRequest.submitTransfer(transfer)

            val currencyCode = transfer.wallet.currency.code
            val transferHash = transfer.hashString()

            breadBox.walletTransfer(currencyCode, transferHash)
                .mapToSendEvent()
                .first()
        }
    }

    private fun handlePostPayment(
        apiClient: APIClient
    ) = flowTransformer<F.PaymentProtocol.PostPayment, E.PaymentProtocol> { effects ->
        effects
            .mapLatest { effect ->
                val paymentRequest = effect.paymentProtocolRequest
                val payment = paymentRequest.createPayment(effect.transfer).orNull()
                checkNotNull(payment) { "failed to create payment" }

                val encodedPayment = payment.encode().orNull()
                checkNotNull(encodedPayment) { "failed to encode payment" }

                val request: Request =
                    Request.Builder().url(paymentRequest.paymentUrl.get()).get()
                        .addHeader(
                            BRConstants.HEADER_ACCEPT, paymentRequest.getAcceptHeader()
                        )
                        .addHeader(
                            BRConstants.HEADER_CONTENT_TYPE,
                            paymentRequest.getContentTypeHeader()
                        )
                        .addHeader(
                            HEADER_BITPAY_PARTNER_KEY, HEADER_BITPAY_PARTNER
                        )
                        .post(encodedPayment.toRequestBody())
                        .build()

                if (apiClient.sendRequest(request, false).isSuccessful) {
                    E.PaymentProtocol.OnPostCompleted
                } else {
                    logWarning("Failed to post payment to bitpay")
                    E.PaymentProtocol.OnPostFailed
                }
            }
    }
}

/**
 * Map the post-submit transfer state to an [E] result or fail.
 */
private fun Flow<Transfer>.mapToSendEvent(): Flow<E> =
    mapNotNull { transfer ->
        when (checkNotNull(transfer.state.type)) {
            TransferState.Type.INCLUDED,
            TransferState.Type.PENDING,
            TransferState.Type.SUBMITTED -> E.OnSendComplete(transfer)
            TransferState.Type.DELETED,
            TransferState.Type.FAILED -> {
                logError("Failed to submit transfer ${transfer.state.failedError.orNull()}")
                E.OnSendFailed
            }
            // Ignore pre-submit states
            TransferState.Type.CREATED,
            TransferState.Type.SIGNED -> null
        }
    }

sealed class Target {
    data class ValidAddress(val address: String) : Target()
    object InvalidAddress : Target()
    object NoAddress : Target()
}
