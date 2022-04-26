/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/13/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.recovery

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.breadwallet.R
import com.breadwallet.app.BreadApp
import com.breadwallet.databinding.ControllerRecoveryKeyBinding
import com.breadwallet.legacy.presenter.customviews.BREdit
import com.breadwallet.tools.animation.BRDialog
import com.breadwallet.tools.animation.SpringAnimator
import com.breadwallet.tools.manager.BRClipboardManager
import com.breadwallet.tools.util.Utils
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.ViewEffect
import com.breadwallet.ui.controllers.AlertDialogController
import com.breadwallet.ui.recovery.RecoveryKey.E
import com.breadwallet.ui.recovery.RecoveryKey.F
import com.breadwallet.ui.recovery.RecoveryKey.M
import com.breadwallet.util.DefaultTextWatcher
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import drewcarlson.mobius.flow.FlowTransformer
import org.kodein.di.direct
import org.kodein.di.instance

class RecoveryKeyController(
    args: Bundle? = null
) : BaseMobiusController<M, E, F>(args),
    AlertDialogController.Listener {

    constructor(
        mode: RecoveryKey.Mode,
        phrase: String? = null
    ) : this(
        bundleOf("mode" to mode.name)
    ) {
        launchPhrase = phrase
        if (launchPhrase != null) {
            eventConsumer.accept(E.OnNextClicked)
        }
    }

    private var launchPhrase: String? = null
    private val mode = arg("mode", RecoveryKey.Mode.RECOVER.name)

    override val defaultModel
        get() = M.createWithOptionalPhrase(
            mode = RecoveryKey.Mode.valueOf(mode),
            phrase = launchPhrase
        )
    override val update = RecoveryKeyUpdate
    override val flowEffectHandler: FlowTransformer<F, E>
        get() = createRecoveryKeyHandler(
            applicationContext as BreadApp,
            direct.instance(),
            direct.instance()
        )

    private val binding by viewBinding(ControllerRecoveryKeyBinding::inflate)

    private val wordInputs: List<BREdit>
        get() = with(binding) {
            listOf(
                word1, word2, word3,
                word4, word5, word6,
                word7, word8, word9,
                word10, word11, word12
            )
        }

    private val inputTextColorValue = TypedValue()
    private var errorTextColor: Int = -1
    private var normalTextColor: Int = -1

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        val theme = view.context.theme
        val resources = resources!!

        theme.resolveAttribute(R.attr.input_words_text_color, inputTextColorValue, true)
        errorTextColor = resources.getColor(R.color.red_text, theme)
        normalTextColor = resources.getColor(inputTextColorValue.resourceId, theme)

        // TODO: This needs a better home
        if (Utils.isUsingCustomInputMethod(applicationContext)) {
            BRDialog.showCustomDialog(
                activity!!,
                resources.getString(R.string.JailbreakWarnings_title),
                resources.getString(R.string.Alert_customKeyboard_android),
                resources.getString(R.string.Button_ok),
                resources.getString(R.string.JailbreakWarnings_close),
                { brDialogView ->
                    val imeManager =
                        applicationContext!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imeManager.showInputMethodPicker()
                    brDialogView.dismissWithAnimation()
                },
                { brDialogView -> brDialogView.dismissWithAnimation() },
                null,
                0
            )
        }
    }

    override fun bindView(output: Consumer<E>): Disposable {
        val resources = resources!!
        with(binding) {
            when (currentModel.mode) {
                RecoveryKey.Mode.WIPE -> {
                    title.text = resources.getString(R.string.RecoveryKeyFlow_enterRecoveryKey)
                    description.text = resources.getString(R.string.WipeWallet_instruction)
                }
                RecoveryKey.Mode.RESET_PIN -> {
                    title.text = resources.getString(R.string.RecoverWallet_header_reset_pin)
                    description.text =
                        resources.getString(R.string.RecoverWallet_subheader_reset_pin)
                }
                RecoveryKey.Mode.RECOVER -> Unit
            }

            faqButton.setOnClickListener {
                output.accept(E.OnFaqClicked)
            }
            sendButton.setOnClickListener {
                output.accept(E.OnNextClicked)
            }
            buttonContactSupport.setOnClickListener {
                output.accept(E.OnContactSupportClicked)
            }
        }

        // Bind paste event
        wordInputs.first().addEditTextEventListener { event ->
            if (event == BREdit.EditTextEvent.PASTE) {
                val clipboardText = BRClipboardManager.getClipboard()
                output.accept(E.OnTextPasted(clipboardText))

                val phrase = clipboardText.split("\\s+".toRegex())
                if (phrase.isNotEmpty()) {
                    wordInputs.zip(phrase)
                        .forEach { (input, word) ->
                            input.setText(word, TextView.BufferType.EDITABLE)
                        }
                }
            }
        }

        // Bind keyboard enter event
        wordInputs.last().setOnEditorActionListener { _, actionId, event ->
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                output.accept(E.OnNextClicked)
            }
            false
        }

        // Bind word input focus event
        wordInputs.forEachIndexed { index, input ->
            input.setOnFocusChangeListener { _, focused ->
                if (focused)
                    output.accept(E.OnFocusedWordChanged(index))
            }
        }

        wordInputs.zip(currentModel.phrase)
            .forEach { (input, word) ->
                input.setText(word, TextView.BufferType.EDITABLE)
            }

        // Bind word input text event
        val watchers = wordInputs.mapIndexed { index, input ->
            createTextWatcher(output, index, input)
        }

        return Disposable {
            wordInputs.zip(watchers)
                .forEach { (input, watcher) ->
                    input.removeTextChangedListener(watcher)
                }
        }
    }

    override fun M.render() {
        ifChanged(M::isLoading) {
            // TODO: Show loading msg
            binding.loadingView.root.isVisible = it
        }

        ifChanged(M::showContactSupport) {
            binding.buttonContactSupport.isVisible = it
        }

        ifChanged(M::errors) { errors ->
            wordInputs.zip(errors)
                .forEach { (input, error) ->
                    if (error) {
                        if (input.currentTextColor != errorTextColor)
                            input.setTextColor(errorTextColor)
                    } else {
                        if (input.currentTextColor != normalTextColor)
                            input.setTextColor(normalTextColor)
                    }
                }
        }
    }

    override fun handleViewEffect(effect: ViewEffect) {
        when (effect) {
            is F.ErrorShake -> SpringAnimator.failShakeAnimation(applicationContext, view)
            is F.WipeWallet ->
                activity?.getSystemService<ActivityManager>()?.clearApplicationUserData()
        }
    }

    override fun onPositiveClicked(
        dialogId: String,
        controller: AlertDialogController,
        result: AlertDialogController.DialogInputResult
    ) {
        when (dialogId) {
            RecoveryKey.DIALOG_WIPE -> eventConsumer.accept(E.OnWipeWalletConfirmed)
        }
    }

    override fun onNegativeClicked(
        dialogId: String,
        controller: AlertDialogController,
        result: AlertDialogController.DialogInputResult
    ) {
        when (dialogId) {
            RecoveryKey.DIALOG_WIPE -> eventConsumer.accept(E.OnWipeWalletCancelled)
        }
    }

    override fun onDismissed(
        dialogId: String,
        controller: AlertDialogController,
        result: AlertDialogController.DialogInputResult
    ) {
        when (dialogId) {
            RecoveryKey.DIALOG_WIPE -> eventConsumer.accept(E.OnWipeWalletCancelled)
        }
    }

    /** Creates a recovery word input text watcher and attaches it to [input]. */
    private fun createTextWatcher(
        output: Consumer<E>,
        index: Int,
        input: EditText
    ) = object : DefaultTextWatcher() {
        override fun afterTextChanged(s: Editable?) {
            val word = s?.toString() ?: ""
            output.accept(E.OnWordChanged(index, word))
        }
    }.also(input::addTextChangedListener)

    override fun handleBack(): Boolean = currentModel.isLoading
}
