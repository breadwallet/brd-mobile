/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 9/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.home

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.breadwallet.BuildConfig
import com.breadwallet.R
import com.breadwallet.databinding.ControllerHomeBinding
import com.breadwallet.legacy.presenter.customviews.BRButton
import com.breadwallet.legacy.presenter.customviews.BREdit
import com.breadwallet.repository.RatesRepository
import com.breadwallet.tools.animation.SpringAnimator
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.controllers.AlertDialogController
import com.breadwallet.ui.formatFiatForUi
import com.breadwallet.ui.home.HomeScreen.E
import com.breadwallet.ui.home.HomeScreen.F
import com.breadwallet.ui.home.HomeScreen.M
import com.breadwallet.util.isValidEmail
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.GenericModelAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.adapters.ModelAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.utils.DragDropUtil
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance

private const val EMAIL_SUCCESS_DELAY = 3_000L
private const val NETWORK_TESTNET = "TESTNET"
private const val NETWORK_MAINNET = "MAINNET"

class HomeController(
    args: Bundle? = null
) : BaseMobiusController<M, E, F>(args), AlertDialogController.Listener {

    override val defaultModel = M.createDefault()
    override val update = HomeScreenUpdate
    override val init = HomeScreenInit
    override val flowEffectHandler
        get() = createHomeScreenHandler(
            context = checkNotNull(applicationContext),
            breadBox = direct.instance(),
            ratesRepo = RatesRepository.getInstance(applicationContext!!),
            brdUser = direct.instance(),
            walletProvider = direct.instance(),
            featurePromotionService = direct.instance(),
            accountMetaDataProvider = direct.instance(),
            connectivityStateProvider = direct.instance(),
            supportManager = direct.instance()
        )

    private val binding by viewBinding(ControllerHomeBinding::inflate)
    private var fastAdapter: GenericFastAdapter? = null
    private var walletAdapter: ModelAdapter<Wallet, WalletListItem>? = null
    private var addWalletAdapter: ItemAdapter<AddWalletItem>? = null
    private var buyDotAnimator: ObjectAnimator? = null
    private var tradeDotAnimator: ObjectAnimator? = null

    override fun bindView(output: Consumer<E>): Disposable {
        return with(binding) {
            buyLayout.setOnClickListener { output.accept(E.OnBuyClicked) }
            tradeLayout.setOnClickListener { output.accept(E.OnTradeClicked) }
            menuLayout.setOnClickListener { output.accept(E.OnMenuClicked) }

            val fastAdapter = checkNotNull(fastAdapter)
            fastAdapter.onClickListener = { _, _, item, _ ->
                val event = when (item) {
                    is AddWalletItem -> E.OnAddWalletsClicked
                    is WalletListItem -> E.OnWalletClicked(item.model.currencyCode)
                    else -> error("Unknown item clicked.")
                }
                output.accept(event)
                true
            }

            Disposable {
                fastAdapter.onClickListener = null
            }
        }
    }

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        setUpBuildInfoLabel()

        walletAdapter = ModelAdapter(::WalletListItem)
        addWalletAdapter = ItemAdapter()

        fastAdapter = FastAdapter.with(listOf(walletAdapter!!, addWalletAdapter!!))

        val dragCallback = SimpleDragCallback(DragEventHandler(fastAdapter!!, eventConsumer))
        val touchHelper = ItemTouchHelper(dragCallback)
        with(binding) {
            touchHelper.attachToRecyclerView(rvWalletList)

            rvWalletList.adapter = fastAdapter
            rvWalletList.itemAnimator = null
            rvWalletList.layoutManager = LinearLayoutManager(view.context)
        }

        addWalletAdapter?.add(AddWalletItem())
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        walletAdapter?.set(currentModel.wallets.values.toList())
    }

    override fun onDestroyView(view: View) {
        walletAdapter = null
        addWalletAdapter = null
        fastAdapter = null
        binding.buyPromoIndicator.removePromoAnimator(buyDotAnimator)
        binding.tradePromoIndicator.removePromoAnimator(tradeDotAnimator)
        super.onDestroyView(view)
    }

    override fun bindView(modelFlow: Flow<M>): Flow<E> {
        modelFlow.distinctUntilChangedBy { it.wallets.values }
            .flowOn(Dispatchers.Default)
            .onEach { m -> walletAdapter?.setNewList(m.wallets.values.toList()) }
            .launchIn(uiBindScope)
        return emptyFlow()
    }

    override fun M.render() {
        with(binding) {
            ifChanged(M::aggregatedFiatBalance) {
                totalAssetsUsd.text =
                    aggregatedFiatBalance.formatFiatForUi(BRSharedPrefs.getPreferredFiatIso())
            }

            ifChanged(M::showPrompt) {
                if (promptContainer.childCount > 0) {
                    promptContainer.removeAllViews()
                }
                if (showPrompt) {
                    val promptView = getPromptView(promptId!!)
                    promptContainer.addView(promptView, 0)
                }
            }

            ifChanged(M::isBuyBellNeeded) {
                buyBell.isVisible = isBuyBellNeeded
            }

            ifChanged(M::hasInternet) {
                notificationBar.apply {
                    isGone = hasInternet
                    if (hasInternet) bringToFront()
                }
                buyTextView.setText(
                    when {
                        showBuyAndSell -> R.string.HomeScreen_buyAndSell
                        else -> R.string.HomeScreen_buy
                    }
                )
            }

            ifChanged(M::isBuyBellNeeded) {
                buyBell.isVisible = isBuyBellNeeded
            }

            ifChanged(M::isBuyPromoDotNeeded) {
                if (isBuyPromoDotNeeded) buyPromoIndicator.animateIndicatorAnimation(true)
            }

            ifChanged(M::isTradePromoDotNeeded) {
                if (isTradePromoDotNeeded) tradePromoIndicator.animateIndicatorAnimation(false)
            }
        }
    }

    private fun setUpBuildInfoLabel() {
        val network = if (BuildConfig.BITCOIN_TESTNET) NETWORK_TESTNET else NETWORK_MAINNET
        val buildInfo = "$network ${BuildConfig.VERSION_NAME} build ${BuildConfig.BUILD_VERSION}"
        binding.testnetLabel.text = buildInfo
        binding.testnetLabel.isVisible = BuildConfig.BITCOIN_TESTNET || BuildConfig.DEBUG
    }

    private fun getPromptView(promptItem: PromptItem): View {
        val act = checkNotNull(activity)

        val baseLayout = act.layoutInflater.inflate(R.layout.base_prompt, null)
        val title = baseLayout.findViewById<TextView>(R.id.prompt_title)
        val description = baseLayout.findViewById<TextView>(R.id.prompt_description)
        val continueButton = baseLayout.findViewById<Button>(R.id.continue_button)
        val dismissButton = baseLayout.findViewById<ImageButton>(R.id.dismiss_button)
        dismissButton.setOnClickListener {
            eventConsumer.accept(E.OnPromptDismissed(promptItem))
        }
        when (promptItem) {
            PromptItem.FINGER_PRINT -> {
                title.text = act.getString(R.string.Prompts_TouchId_title_android)
                description.text = act.getString(R.string.Prompts_TouchId_body_android)
                continueButton.setOnClickListener {
                    eventConsumer.accept(E.OnFingerprintPromptClicked)
                }
            }
            PromptItem.PAPER_KEY -> {
                title.text = act.getString(R.string.Prompts_PaperKey_title)
                description.text = act.getString(R.string.Prompts_PaperKey_Body_Android)
                continueButton.setOnClickListener {
                    eventConsumer.accept(E.OnPaperKeyPromptClicked)
                }
            }
            PromptItem.UPGRADE_PIN -> {
                title.text = act.getString(R.string.Prompts_UpgradePin_title)
                description.text = act.getString(R.string.Prompts_UpgradePin_body)
                continueButton.setOnClickListener {
                    eventConsumer.accept(E.OnUpgradePinPromptClicked)
                }
            }
            PromptItem.RECOMMEND_RESCAN -> {
                title.text = act.getString(R.string.Prompts_RecommendRescan_title)
                description.text = act.getString(R.string.Prompts_RecommendRescan_body)
                continueButton.setOnClickListener {
                    eventConsumer.accept(E.OnRescanPromptClicked)
                }
            }
            PromptItem.EMAIL_COLLECTION -> {
                return getEmailPrompt()
            }
            PromptItem.RATE_APP -> return getRateAppPrompt()
        }
        return baseLayout
    }

    private fun getEmailPrompt(): View {
        val act = checkNotNull(activity)
        val customLayout = act.layoutInflater.inflate(R.layout.email_prompt, null)
        val customTitle = customLayout.findViewById<TextView>(R.id.prompt_title)
        val customDescription =
            customLayout.findViewById<TextView>(R.id.prompt_description)
        val footNote = customLayout.findViewById<TextView>(R.id.prompt_footnote)
        val submitButton = customLayout.findViewById<BRButton>(R.id.submit_button)
        val closeButton = customLayout.findViewById<ImageView>(R.id.close_button)
        val emailEditText = customLayout.findViewById<BREdit>(R.id.email_edit)
        submitButton.setColor(act.getColor(R.color.create_new_wallet_button_dark))
        customTitle.text = act.getString(R.string.Prompts_Email_title)
        customDescription.text = act.getString(R.string.Prompts_Email_body)
        closeButton.setOnClickListener {
            eventConsumer.accept(E.OnPromptDismissed(PromptItem.EMAIL_COLLECTION))
        }
        submitButton.setOnClickListener {
            val email = emailEditText.text.toString().trim { it <= ' ' }
            if (email.isValidEmail()) {
                eventConsumer.accept(E.OnEmailPromptClicked(email))
                emailEditText.visibility = View.INVISIBLE
                submitButton.visibility = View.INVISIBLE
                footNote.visibility = View.VISIBLE
                customTitle.text = act.getString(R.string.Prompts_Email_successTitle)
                customDescription.text = act.getString(R.string.Prompts_Email_successBody)
                viewAttachScope.launch(Main) {
                    delay(EMAIL_SUCCESS_DELAY)
                    binding.promptContainer.removeAllViews()
                }
            } else {
                SpringAnimator.failShakeAnimation(act, emailEditText)
            }
        }
        return customLayout
    }

    private fun getRateAppPrompt(): View {
        val act = checkNotNull(activity)
        val customLayout = act.layoutInflater.inflate(R.layout.rate_app_prompt, null)
        val negativeButton = customLayout.findViewById<BRButton>(R.id.negative_button)
        val positiveButton = customLayout.findViewById<BRButton>(R.id.submit_button)
        val closeButton = customLayout.findViewById<ImageView>(R.id.close_button)
        val dontShowCheckBox = customLayout.findViewById<CheckBox>(R.id.dont_show_checkbox)
        closeButton.setOnClickListener {
            eventConsumer.accept(E.OnPromptDismissed(PromptItem.RATE_APP))
        }
        negativeButton.setOnClickListener {
            eventConsumer.accept(E.OnPromptDismissed(PromptItem.RATE_APP))
            eventConsumer.accept(E.OnRateAppPromptNoThanksClicked)
        }
        positiveButton.setOnClickListener {
            eventConsumer.accept(E.OnRateAppPromptClicked)
        }
        dontShowCheckBox.setOnClickListener {
            eventConsumer.accept(E.OnRateAppPromptDontShowClicked((it as CheckBox).isChecked))
        }
        return customLayout
    }

    private class DragEventHandler(
        private val fastAdapter: GenericFastAdapter,
        private val output: Consumer<E>
    ) : ItemTouchCallback {

        fun isAddWallet(position: Int) = fastAdapter.getItem(position) is AddWalletItem

        override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
            if (isAddWallet(newPosition)) return false

            val adapter = fastAdapter.getAdapter(newPosition)
            check(adapter is GenericModelAdapter<*>)
            DragDropUtil.onMove(adapter, oldPosition, newPosition)

            output.accept(
                E.OnWalletDisplayOrderUpdated(
                    adapter.models
                        .filterIsInstance<Wallet>()
                        .map(Wallet::currencyId)
                )
            )
            return true
        }

        override fun itemTouchDropped(oldPosition: Int, newPosition: Int) = Unit
    }

    override fun onPositiveClicked(
        dialogId: String,
        controller: AlertDialogController,
        result: AlertDialogController.DialogInputResult
    ) {
        eventConsumer.accept(E.OnSupportFormSubmitted(result.inputText))
    }

    private fun ImageView.animateIndicatorAnimation(isBuyTextField: Boolean) {
        isVisible = true
        if (isBuyTextField) {
            buyDotAnimator = getDotAnimator(drawable)
            buyDotAnimator?.start()
        } else {
            tradeDotAnimator = getDotAnimator(drawable)
            tradeDotAnimator?.start()
        }
    }

    private fun ImageView.removePromoAnimator(animator: ObjectAnimator?) {
        isVisible = false
        animator?.removeAllUpdateListeners()
        animator?.cancel()
    }

    private fun getDotAnimator(drawable: Drawable?) =
        ObjectAnimator.ofPropertyValuesHolder(
            drawable,
            PropertyValuesHolder.ofInt("alpha", 0, 255)
        ).apply {
            duration = 2000L
            target = drawable
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
}
