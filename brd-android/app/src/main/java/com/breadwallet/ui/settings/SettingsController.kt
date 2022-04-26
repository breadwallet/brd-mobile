/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.breadwallet.R
import com.breadwallet.databinding.ControllerSettingsBinding
import com.breadwallet.tools.util.Link
import com.breadwallet.tools.util.ServerBundlesHelper
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.ViewEffect
import com.breadwallet.ui.auth.AuthenticationController
import com.breadwallet.ui.controllers.AlertDialogController
import com.breadwallet.ui.flowbind.clicks
import com.breadwallet.ui.scanner.ScannerController
import com.breadwallet.ui.settings.SettingsScreen.E
import com.breadwallet.ui.settings.SettingsScreen.F
import com.breadwallet.ui.settings.SettingsScreen.M
import com.platform.APIClient
import com.spotify.mobius.Connectable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.kodein.di.direct
import org.kodein.di.instance

private const val HIDDEN_MENU_CLICKS = 5

class SettingsController(
    args: Bundle? = null
) : BaseMobiusController<M, E, F>(args),
    ScannerController.Listener,
    AuthenticationController.Listener,
    AlertDialogController.Listener {

    companion object {
        private const val EXT_SECTION = "section"
    }

    constructor(section: SettingsSection) : this(
        bundleOf(
            EXT_SECTION to section.name
        )
    )

    private val section: SettingsSection = SettingsSection.valueOf(arg(EXT_SECTION))

    init {
        if (section != SettingsSection.HOME) {
            overridePopHandler(HorizontalChangeHandler())
            overridePushHandler(HorizontalChangeHandler())
        }
    }

    override val init = SettingsInit
    override val defaultModel = M.createDefault(section)
    override val update = SettingsUpdate
    override val effectHandler = Connectable<F, E> { output ->
        SettingsScreenHandler(
            output = output,
            context = applicationContext!!,
            experimentsRepository = direct.instance(),
            metaDataManager = direct.instance(),
            userManager = direct.instance(),
            breadBox = direct.instance(),
            bdbAuthInterceptor = direct.instance(),
            supportManager = direct.instance(),
            brdPreferences = direct.instance(),
            brdClient = direct.instance(),
            scope = direct.instance()
        )
    }

    private val binding by viewBinding(ControllerSettingsBinding::inflate)

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        binding.settingsList.layoutManager = LinearLayoutManager(activity!!)
        binding.settingsList.addItemDecoration(
            DividerItemDecoration(
                activity!!,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun bindView(modelFlow: Flow<M>): Flow<E> {
        return with(binding) {
            merge(
                closeButton.clicks().map { E.OnCloseClicked },
                backButton.clicks().map { E.OnBackClicked },
                title.clicks()
                    .dropWhile { currentModel.section != SettingsSection.HOME }
                    .drop(HIDDEN_MENU_CLICKS)
                    .map { E.ShowHiddenOptions }
            )
        }
    }

    override fun M.render() {
        val act = activity!!
        with(binding) {
            ifChanged(M::section) {
                title.text = when (section) {
                    SettingsSection.HOME -> act.getString(R.string.Settings_title)
                    SettingsSection.PREFERENCES -> act.getString(R.string.Settings_preferences)
                    SettingsSection.HIDDEN,
                    SettingsSection.DEVELOPER_OPTION -> "Developer Options"
                    SettingsSection.SECURITY -> act.getString(R.string.MenuButton_security)
                    SettingsSection.BTC_SETTINGS -> "Bitcoin ${act.getString(R.string.Settings_title)}"
                    SettingsSection.BCH_SETTINGS -> "Bitcoin Cash ${act.getString(R.string.Settings_title)}"
                }
                val isHome = section == SettingsSection.HOME
                closeButton.isVisible = isHome
                backButton.isVisible = !isHome
            }
            ifChanged(M::items) {
                val adapter = SettingsAdapter(items) { option ->
                    eventConsumer.accept(E.OnOptionClicked(option))
                }
                settingsList.adapter = adapter
            }
            ifChanged(M::isLoading) {
                loadingView.root.isVisible = isLoading
            }
        }
    }

    override fun handleViewEffect(effect: ViewEffect) {
        when (effect) {
            F.ShowApiServerDialog -> showApiServerDialog(APIClient.host)
            F.ShowPlatformDebugUrlDialog -> showPlatformDebugUrlDialog(
                ServerBundlesHelper.getWebPlatformDebugURL()
            )
            F.ShowTokenBundleDialog -> showTokenBundleDialog(
                ServerBundlesHelper.getBundle(ServerBundlesHelper.Type.TOKEN)
            )
            F.ShowPlatformBundleDialog -> showPlatformBundleDialog(
                ServerBundlesHelper.getBundle(ServerBundlesHelper.Type.WEB)
            )
            is F.ExportTransactions -> exportTransactions(effect.uri)
        }
    }

    override fun onLinkScanned(link: Link) {
        eventConsumer.accept(E.OnLinkScanned(link))
    }

    override fun onAuthenticationSuccess() {
        eventConsumer.accept(E.OnAuthenticated)
    }

    override fun onPositiveClicked(
        dialogId: String,
        controller: AlertDialogController,
        result: AlertDialogController.DialogInputResult
    ) {
        eventConsumer.accept(E.OnExportTransactionsConfirmed)
    }

    /** Developer options dialogs */

    private fun showApiServerDialog(host: String) {
        showInputTextDialog("API Server:", host) { newHost ->
            eventConsumer.accept(E.SetApiServer(newHost))
        }
    }

    private fun showPlatformDebugUrlDialog(url: String) {
        showInputTextDialog("Platform debug url:", url) { newUrl ->
            eventConsumer.accept(E.SetPlatformDebugUrl(newUrl))
        }
    }

    private fun showPlatformBundleDialog(platformBundle: String) {
        showInputTextDialog("Platform Bundle:", platformBundle) { newBundle ->
            eventConsumer.accept(E.SetPlatformBundle(newBundle))
        }
    }

    private fun showTokenBundleDialog(tokenBundle: String) {
        showInputTextDialog("Token Bundle:", tokenBundle) { newBundle ->
            eventConsumer.accept(E.SetTokenBundle(newBundle))
        }
    }

    private fun showInputTextDialog(
        message: String,
        currentValue: String,
        onConfirmation: (String) -> Unit
    ) {
        val act = checkNotNull(activity)
        val editText = EditText(act)
        editText.setText(currentValue, TextView.BufferType.EDITABLE)
        AlertDialog.Builder(act)
            .setMessage(message)
            .setView(editText)
            .setPositiveButton(R.string.Button_confirm) { _, _ ->
                val platformURL = editText.text.toString()
                onConfirmation(platformURL)
            }
            .setNegativeButton(R.string.Button_cancel, null)
            .create()
            .show()
    }

    private fun exportTransactions(uri: Uri) {
        val context = checkNotNull(activity)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.Settings_exportTransfers))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val title = context.getString(R.string.Settings_share)
        val chooserIntent = Intent.createChooser(shareIntent, title)
        chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(chooserIntent)
    }
}
