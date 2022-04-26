/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/14/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.nodeselector

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.breadwallet.R
import com.breadwallet.databinding.ControllerNodeSelectorBinding
import com.breadwallet.tools.util.getPixelsFromDps
import com.breadwallet.mobius.CompositeEffectHandler
import com.breadwallet.tools.util.TrustedNode
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.ViewEffect
import com.breadwallet.ui.flowbind.clicks
import com.breadwallet.ui.settings.nodeselector.NodeSelector.E
import com.breadwallet.ui.settings.nodeselector.NodeSelector.F
import com.breadwallet.ui.settings.nodeselector.NodeSelector.M
import com.spotify.mobius.Connectable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance

private const val DIALOG_TITLE_PADDING = 16
private const val DIALOG_TITLE_TEXT_SIZE = 18f
private const val DIALOG_INPUT_PADDING = 24
private const val SHOW_KEYBOARD_DELAY = 200L
private const val RESTORE_DIALOG_TITLE_DELAY = 1_000L

class NodeSelectorController : BaseMobiusController<M, E, F>() {

    override val defaultModel = M.createDefault()
    override val update = NodeSelectorUpdate
    override val init = NodeSelectorInit

    override val effectHandler = CompositeEffectHandler.from<F, E>(
        Connectable { output ->
            NodeSelectorHandler(output, direct.instance())
        }
    )

    private val binding by viewBinding(ControllerNodeSelectorBinding::inflate)

    override fun bindView(modelFlow: Flow<M>): Flow<E> {
        return merge(
            binding.buttonSwitch.clicks().map { E.OnSwitchButtonClicked }
        )
    }

    override fun M.render() {
        val res = checkNotNull(resources)
        with(binding) {
            ifChanged(M::mode) {
                buttonSwitch.text = when (mode) {
                    NodeSelector.Mode.AUTOMATIC -> res.getString(R.string.NodeSelector_manualButton)
                    NodeSelector.Mode.MANUAL -> res.getString(R.string.NodeSelector_automaticButton)
                    else -> ""
                }
            }

            ifChanged(M::currentNode) {
                nodeText.text = if (currentNode.isNotBlank()) {
                    currentNode
                } else {
                    res.getString(R.string.NodeSelector_automatic)
                }
            }

            ifChanged(M::connected) {
                nodeStatus.text = if (connected) {
                    res.getString(R.string.NodeSelector_connected)
                } else {
                    res.getString(R.string.NodeSelector_notConnected)
                }
            }
        }
    }

    override fun handleViewEffect(effect: ViewEffect) {
        when (effect) {
            F.ShowNodeDialog -> showNodeDialog()
        }
    }

    private fun showNodeDialog() {
        val res = checkNotNull(resources)
        val alertDialog = AlertDialog.Builder(activity)
        val customTitle = TextView(activity)

        customTitle.gravity = Gravity.CENTER
        customTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER
        val pad16 = activity?.getPixelsFromDps(DIALOG_TITLE_PADDING) ?: 0
        customTitle.setPadding(pad16, pad16, pad16, pad16)
        customTitle.text = res.getString(R.string.NodeSelector_enterTitle)
        customTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, DIALOG_TITLE_TEXT_SIZE)
        customTitle.setTypeface(null, Typeface.BOLD)
        alertDialog.setCustomTitle(customTitle)
        alertDialog.setMessage(res.getString(R.string.NodeSelector_enterBody))

        val input = EditText(activity)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val padding = activity?.getPixelsFromDps(DIALOG_INPUT_PADDING) ?: 0

        input.setPadding(padding, 0, padding, padding)
        input.layoutParams = lp
        alertDialog.setView(input)

        alertDialog.setNegativeButton(
            res.getString(R.string.Button_cancel)
        ) { dialog, _ -> dialog.cancel() }

        alertDialog.setPositiveButton(
            res.getString(R.string.Button_ok)
        ) { _, _ ->
            // this implementation will be overridden
        }

        val dialog = alertDialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val node = input.text.toString()
            if (TrustedNode.isValid(node)) {
                eventConsumer.accept(E.SetCustomNode(node))
                dialog.dismiss()
            } else {
                viewAttachScope.launch(Dispatchers.Main) {
                    customTitle.setText(R.string.NodeSelector_invalid)
                    customTitle.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.warning_color)
                    )
                    delay(RESTORE_DIALOG_TITLE_DELAY)
                    customTitle.setText(R.string.NodeSelector_enterTitle)
                    customTitle.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.almost_black)
                    )
                }
            }
        }
        viewAttachScope.launch(Dispatchers.Main) {
            delay(SHOW_KEYBOARD_DELAY)
            input.requestFocus()
            val keyboard =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(input, 0)
        }
    }
}
