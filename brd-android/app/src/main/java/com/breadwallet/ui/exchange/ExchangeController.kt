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
import android.view.View
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import com.brd.api.models.ExchangeCurrency
import com.brd.api.models.ExchangeOrder
import com.brd.exchange.*
import com.brd.exchange.ExchangeModel.Mode.*
import com.brd.exchange.ExchangeModel.OfferDetails
import com.breadwallet.BuildConfig
import com.breadwallet.R
import com.breadwallet.breadbox.formatCryptoForUi
import com.breadwallet.databinding.ControllerExchangeBinding
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.MobiusKtController
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kt.mobius.Connectable
import kt.mobius.Connection
import kt.mobius.disposables.Disposable
import kt.mobius.extras.CompositeEffectHandler
import kt.mobius.functions.Consumer
import org.kodein.di.direct
import org.kodein.di.instance
import java.util.*

private const val KEY_MODE = "ExchangeController.KEY_MODE"

class ExchangeController(args: Bundle) :
    MobiusKtController<ExchangeModel, ExchangeEvent, ExchangeEffect>(args) {

    constructor() : this(bundleOf())
    constructor(mode: ExchangeModel.Mode) : this(bundleOf(KEY_MODE to mode.name))

    private val binding by viewBinding(ControllerExchangeBinding::inflate)

    override val defaultModel = argOptional<String>(KEY_MODE)
        ?.run(ExchangeModel.Mode::valueOf)
        ?.let { mode ->
            ExchangeModel.create(
                mode = mode,
                test = BuildConfig.BITCOIN_TESTNET,
            )
        } ?: ExchangeModel.createForSettings()
    override val update = ExchangeUpdate
    override val init = ExchangeInit
    override val effectHandler
        get() = CompositeEffectHandler.from(
            object : Connectable<ExchangeEffect, ExchangeEvent> {
                override fun connect(output: Consumer<ExchangeEvent>): Connection<ExchangeEffect> {
                    return ExchangeEffectHandler(
                        output,
                        direct.instance(),
                        direct.instance(),
                        AndroidWalletProvider(direct.instance(), direct.instance()),
                        direct.instance(),
                        Dispatchers.Default
                    )
                }
            },
            object : Connectable<ExchangeEffect, ExchangeEvent> {
                override fun connect(output: Consumer<ExchangeEvent>): Connection<ExchangeEffect> {
                    return AndroidExchangeEffectHandler(router)
                }
            },
            object : Connectable<ExchangeEffect, ExchangeEvent> {
                override fun connect(output: Consumer<ExchangeEvent>): Connection<ExchangeEffect> {
                    return object : Connection<ExchangeEffect> {
                        val scope = CoroutineScope(Main + SupervisorJob())
                        override fun accept(value: ExchangeEffect) {
                            scope.launch(Main) {
                                val childRouter = getChildRouter(binding.exchangeRoot)
                                val topController = childRouter.backstack.lastOrNull()?.controller
                                (topController as? ChildController)?.handleEffect(value)
                            }
                        }

                        override fun dispose() {
                            scope.cancel()
                        }
                    }
                }
            }
        )

    override fun bindView(output: Consumer<ExchangeEvent>): Disposable = with(binding) {
        dialog.posButton.setOnClickListener {
            output.accept(ExchangeEvent.OnDialogConfirmClicked)
        }
        dialog.negButton.setOnClickListener {
            output.accept(ExchangeEvent.OnDialogCancelClicked)
        }

        Disposable {
        }
    }

    override fun ExchangeModel.render(): Unit = with(binding) {
        val res = requireResources()
        val childRouter = getChildRouter(exchangeRoot)

        val rootController = childRouter.backstack.firstOrNull()?.controller
        val topController = childRouter.backstack.lastOrNull()?.controller

        ifChanged(ExchangeModel::state) {
            when (val state = state) {
                ExchangeModel.State.EmptyWallets -> {
                    if (topController !is EmptyWalletsController) {
                        childRouter.setRoot(
                            RouterTransaction.with(EmptyWalletsController())
                                .popChangeHandler(FadeChangeHandler())
                                .pushChangeHandler(FadeChangeHandler())
                        )
                    }
                }
                is ExchangeModel.State.Initializing -> {
                    val matchesMode = (mode == BUY && topController is BuyController) ||
                        (mode == TRADE && topController is TradeController)
                    if (topController == null || !matchesMode) {
                        val transaction = when (mode) {
                            BUY -> RouterTransaction.with(BuyController())
                            TRADE -> RouterTransaction.with(TradeController())
                            SELL -> error("SELL not implemented")
                        }.pushChangeHandler(VerticalChangeHandler())
                            .popChangeHandler(VerticalChangeHandler())
                        childRouter.setRoot(transaction)
                    } else {
                        childRouter.popToRoot()
                    }
                }
                is ExchangeModel.State.OrderSetup -> {
                    val shouldUpdateRoot = (mode == BUY && rootController !is BuyController) ||
                        (mode == TRADE && rootController !is TradeController)
                    if (state.selectingOffer) {
                        val currentSelectionType = (topController as? PickerController)?.selectionType
                        if (currentSelectionType != PickerController.SelectionType.OFFER) {
                            val (pushHandler, popHandler) = when (mode) {
                                BUY,
                                SELL -> HorizontalChangeHandler() to HorizontalChangeHandler()
                                TRADE -> VerticalChangeHandler() to VerticalChangeHandler()
                            }
                            val transaction =
                                RouterTransaction.with(PickerController(PickerController.SelectionType.OFFER))
                                    .pushChangeHandler(pushHandler)
                                    .popChangeHandler(popHandler)
                            childRouter.pushController(transaction)
                        }
                    } else if (shouldUpdateRoot) {
                        val transaction = when (mode) {
                            BUY -> RouterTransaction.with(BuyController())
                            TRADE -> RouterTransaction.with(TradeController())
                            SELL -> error("SELL not implemented")
                        }.pushChangeHandler(VerticalChangeHandler())
                            .popChangeHandler(VerticalChangeHandler())
                        childRouter.setRoot(transaction)
                        childRouter.popToRoot()
                    } else if (childRouter.backstackSize > 1) {
                        childRouter.popToRoot()
                    }
                }
                is ExchangeModel.State.ConfigureSettings -> {
                    val currentSelectionType = (topController as? PickerController)?.selectionType
                    when (state.target) {
                        ExchangeModel.ConfigTarget.MENU -> {
                            if (topController !is ExchangeSettingsController) {
                                val hasSettingsController = childRouter.backstack
                                    .map(RouterTransaction::controller)
                                    .any { it is ExchangeSettingsController }

                                if (hasSettingsController) {
                                    childRouter.popCurrentController()
                                } else {
                                    val transaction = RouterTransaction.with(ExchangeSettingsController())
                                        .pushChangeHandler(VerticalChangeHandler())
                                        .popChangeHandler(VerticalChangeHandler())
                                    childRouter.pushController(transaction)
                                }
                            }
                        }
                        ExchangeModel.ConfigTarget.CURRENCY -> {
                            if (currentSelectionType != PickerController.SelectionType.CURRENCY) {
                                val transaction =
                                    RouterTransaction.with(PickerController(PickerController.SelectionType.CURRENCY))
                                        .pushChangeHandler(HorizontalChangeHandler())
                                        .popChangeHandler(HorizontalChangeHandler())
                                if (currentSelectionType == null) {
                                    childRouter.pushController(transaction)
                                } else {
                                    childRouter.replaceTopController(transaction)
                                }
                            }
                        }
                        ExchangeModel.ConfigTarget.COUNTRY -> {
                            if (currentSelectionType != PickerController.SelectionType.COUNTRY) {
                                val transaction =
                                    RouterTransaction.with(PickerController(PickerController.SelectionType.COUNTRY))
                                        .pushChangeHandler(HorizontalChangeHandler())
                                        .popChangeHandler(HorizontalChangeHandler())
                                if (currentSelectionType == null) {
                                    childRouter.pushController(transaction)
                                } else {
                                    childRouter.replaceTopController(transaction)
                                }
                            }
                        }
                        ExchangeModel.ConfigTarget.REGION -> {
                            if (currentSelectionType != PickerController.SelectionType.REGION) {
                                val transaction =
                                    RouterTransaction.with(PickerController(PickerController.SelectionType.REGION))
                                        .pushChangeHandler(HorizontalChangeHandler())
                                        .popChangeHandler(HorizontalChangeHandler())
                                if (currentSelectionType == null) {
                                    childRouter.pushController(transaction)
                                } else {
                                    childRouter.replaceTopController(transaction)
                                }
                            }
                        }
                    }
                }
                is ExchangeModel.State.SelectAsset -> {
                    val currentSelectionType = (topController as? PickerController)?.selectionType
                    if (currentSelectionType != PickerController.SelectionType.ASSET) {
                        val transaction = RouterTransaction.with(PickerController(PickerController.SelectionType.ASSET))
                            .pushChangeHandler(HorizontalChangeHandler())
                            .popChangeHandler(HorizontalChangeHandler())
                        if (currentSelectionType == null) {
                            childRouter.pushController(transaction)
                        } else {
                            childRouter.replaceTopController(transaction)
                        }
                    }
                }
                is ExchangeModel.State.CreatingOrder -> {
                    if (state.previewing) {
                        if (topController !is OrderPreviewController) {
                            val transaction = RouterTransaction.with(OrderPreviewController())
                                .pushChangeHandler(VerticalChangeHandler())
                                .popChangeHandler(VerticalChangeHandler())
                            childRouter.pushController(transaction)
                        }
                    } else if (topController is OrderPreviewController) {
                        childRouter.popController(topController)
                    }
                }
                is ExchangeModel.State.ProcessingOrder -> {
                    if (state.userAction == null) {
                        if (mode == TRADE && topController !is TradeTransactionController) {
                            val transaction = RouterTransaction.with(TradeTransactionController())
                                .pushChangeHandler(FadeChangeHandler(false))
                                .popChangeHandler(FadeChangeHandler())
                            childRouter.pushController(transaction)
                        }
                    } else {
                        state.userAction?.run {
                            when (action.type) {
                                ExchangeOrder.Action.Type.CRYPTO_REFUND_ADDRESS,
                                ExchangeOrder.Action.Type.CRYPTO_RECEIVE_ADDRESS -> error("Invalid user action type")
                                ExchangeOrder.Action.Type.CRYPTO_SEND -> Unit
                                ExchangeOrder.Action.Type.BROWSER -> {
                                    if (topController !is PartnerBrowserController) {
                                        val transaction = RouterTransaction.with(PartnerBrowserController())
                                            .pushChangeHandler(HorizontalChangeHandler())
                                            .popChangeHandler(HorizontalChangeHandler())
                                        childRouter.pushController(transaction)
                                    }
                                }
                            }
                        }
                    }
                }
                is ExchangeModel.State.OrderComplete -> {
                    if (topController !is OrderCompleteController) {
                        val transaction = RouterTransaction.with(OrderCompleteController())
                            .pushChangeHandler(FadeChangeHandler())
                            .popChangeHandler(HorizontalChangeHandler())
                        childRouter.pushController(transaction)
                    }
                }
            }
        }

        ifChanged(ExchangeModel::confirmingClose, ExchangeModel::errorState) {
            if (confirmingClose) {
                dialog.dialogTitle.setText(R.string.Exchange_tradeCancelAlertTitle)
                dialog.dialogText.setText(R.string.Exchange_tradeCancelAlertBody)
                dialog.posButton.setText(R.string.Exchange_tradeCancelAlertYes)
                dialog.negButton.setText(R.string.Exchange_tradeCancelAlertNo)
                dialog.helpIcon.isVisible = false
            }

            val errorState = errorState
            if (errorState != null) {
                if (errorState.isRecoverable) {
                    dialog.posButton.setText(R.string.Exchange_CTA_retry)
                    dialog.negButton.setText(R.string.Button_cancel)
                } else {
                    dialog.posButton.setText(R.string.Button_ok)
                    dialog.negButton.isVisible = false
                }

                dialog.helpIcon.isVisible = false
                dialog.dialogTitle.isVisible = false
                when (val type = errorState.type) {
                    is ExchangeModel.ErrorState.Type.TransactionError -> {
                        when (type.sendFailedReason) {
                            ExchangeEvent.SendFailedReason.FeeEstimateFailed -> {
                                dialog.dialogText.setText(R.string.Send_noFeesError)
                            }
                            else -> {
                                dialog.dialogText.setText(R.string.Exchange_ErrorState_transaction)
                            }
                        }
                    }
                    is ExchangeModel.ErrorState.Type.OrderError -> {
                        dialog.dialogText.setText(R.string.Exchange_ErrorState_order)
                    }
                    is ExchangeModel.ErrorState.Type.NetworkError -> {
                        dialog.dialogText.setText(R.string.Exchange_ErrorState_network)
                    }
                    is ExchangeModel.ErrorState.Type.InitializationError -> {
                        dialog.dialogText.setText(R.string.Exchange_ErrorState_initialization)
                    }
                    is ExchangeModel.ErrorState.Type.UnsupportedRegionError -> {
                        dialog.dialogText.setText(R.string.Exchange_ErrorState_unsupportedRegionError)
                    }
                    is ExchangeModel.ErrorState.Type.InsufficientNativeBalanceError -> {
                        dialog.posButton.setText(R.string.Exchange_ErrorState_insufficientNativeBalanceErrorConfirm)
                        dialog.dialogTitle.isVisible = true
                        dialog.dialogTitle.setText(R.string.Send_insufficientGasTitle)
                        dialog.dialogText.text = res.getString(
                            R.string.Send_insufficientGasMessage,
                            type.amount.toBigDecimal().formatCryptoForUi(type.currencyCode)
                        )
                    }
                    is ExchangeModel.ErrorState.Type.UnknownError -> {
                        dialog.dialogText.setText(R.string.Exchange_ErrorState_unknown)
                    }
                }
            }

            layoutDialog.isVisible = confirmingClose || errorState != null
        }

        (childRouter.backstack.lastOrNull()?.controller as? ChildController)?.dispatchRender()
    }

    abstract class ChildController(args: Bundle? = null) : BaseController(args) {
        val eventConsumer: Consumer<ExchangeEvent>
            get() = (parentController as ExchangeController).eventConsumer

        val currentModel: ExchangeModel
            get() = (parentController as ExchangeController).currentModel

        var previousModel: ExchangeModel? = null
            private set

        override fun onAttach(view: View) {
            super.onAttach(view)
            previousModel = null
            dispatchRender()
        }

        fun dispatchRender() {
            currentModel.render()
            previousModel = currentModel
        }

        open fun handleEffect(effect: ExchangeEffect) = Unit

        abstract fun ExchangeModel.render()

        inline fun <T> ExchangeModel.ifChanged(
            crossinline extract: (ExchangeModel) -> T,
            crossinline block: (@ParameterName("value") T) -> Unit
        ) {
            val currentValue = extract(this)
            val previousValue: Any? = if (previousModel == null) Unit else previousModel?.run(extract)
            if (currentValue != previousValue) {
                block(currentValue)
            }
        }

        inline fun <T1, T2> ExchangeModel.ifChanged(
            crossinline extract1: (ExchangeModel) -> T1,
            crossinline extract2: (ExchangeModel) -> T2,
            crossinline block: () -> Unit
        ) {
            if (
                extract1(this) != (if (previousModel == null) Unit else previousModel?.run(extract1)) ||
                extract2(this) != (if (previousModel == null) Unit else previousModel?.run(extract2))
            ) {
                block()
            }
        }

        inline fun <T1, T2, T3> ExchangeModel.ifChanged(
            crossinline extract1: (ExchangeModel) -> T1,
            crossinline extract2: (ExchangeModel) -> T2,
            crossinline extract3: (ExchangeModel) -> T3,
            crossinline block: () -> Unit
        ) {
            if (
                extract1(this) != (if (previousModel == null) Unit else previousModel?.run(extract1)) ||
                extract2(this) != (if (previousModel == null) Unit else previousModel?.run(extract2)) ||
                extract3(this) != (if (previousModel == null) Unit else previousModel?.run(extract3))
            ) {
                block()
            }
        }

        override fun handleBack(): Boolean {
            eventConsumer.accept(ExchangeEvent.OnBackClicked)
            return true
        }
    }
}

fun ExchangeCurrency.selectedFiatCurrencyName(): String {
    val currency = Currency.getInstance(code)
    return "${code.toUpperCase(Locale.ROOT)} (${currency?.symbol}) - $name"
}

fun OfferDetails.setProviderIcon(icon: ImageView) {
    when (offer.provider.slug.removeSuffix("-test")) {
        "moonpay" -> icon.setImageResource(R.drawable.ic_provider_moonpay)
        "simplex" -> icon.setImageResource(R.drawable.ic_provider_simplex)
        "wyre" -> icon.setImageResource(R.drawable.ic_provider_wyre)
        "changelly" -> icon.setImageResource(R.drawable.ic_proivider_changelly)
        else -> {
            if (offer.provider.logoUrl.isNullOrBlank()) {
                icon.setImageDrawable(null)
            } else {
                Picasso.get().load(offer.provider.logoUrl).into(icon)
            }
        }
    }
}
