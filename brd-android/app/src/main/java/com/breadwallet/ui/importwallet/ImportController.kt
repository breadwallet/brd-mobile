/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 12/3/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.importwallet

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.RouterTransaction
import com.breadwallet.R
import com.breadwallet.databinding.ControllerImportWalletBinding
import com.breadwallet.tools.util.Link
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.ViewEffect
import com.breadwallet.ui.controllers.AlertDialogController
import com.breadwallet.ui.flowbind.clicks
import com.breadwallet.ui.importwallet.Import.CONFIRM_IMPORT_DIALOG
import com.breadwallet.ui.importwallet.Import.E
import com.breadwallet.ui.importwallet.Import.F
import com.breadwallet.ui.importwallet.Import.IMPORT_SUCCESS_DIALOG
import com.breadwallet.ui.importwallet.Import.M
import com.breadwallet.ui.scanner.ScannerController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

private const val PRIVATE_KEY = "ImportController.private_key"
private const val PASSWORD_PROTECTED = "ImportController.password_protected"
private const val RECLAIMING_GIFT = "ImportController.reclaim_gift_hash"
private const val SCANNED = "ImportController.scanned"
private const val GIFT = "ImportController.gift"

@Suppress("TooManyFunctions")
class ImportController(
    args: Bundle? = null
) : BaseMobiusController<M, E, F>(args),
    ScannerController.Listener,
    AlertDialogController.Listener,
    PasswordController.Listener {

    constructor(
        privateKey: String,
        isPasswordProtected: Boolean,
        reclaimingGift: String? = null,
        scanned: Boolean,
        gift: Boolean,
    ) : this(
        bundleOf(
            PRIVATE_KEY to privateKey,
            PASSWORD_PROTECTED to isPasswordProtected,
            RECLAIMING_GIFT to reclaimingGift,
            SCANNED to scanned,
            GIFT to gift
        )
    )

    override val init = ImportInit
    override val update = ImportUpdate
    override val defaultModel = M.createDefault(
        privateKey = argOptional(PRIVATE_KEY),
        isPasswordProtected = arg(PASSWORD_PROTECTED, false),
        reclaimGiftHash = argOptional(RECLAIMING_GIFT),
        scanned = arg(SCANNED, false)
    )

    override val di by DI.lazy {
        extend(super.di)

        bind<WalletImporter>() with singleton { WalletImporter() }
    }

    override val flowEffectHandler
        get() = createImportHandler(
            direct.instance(),
            direct.instance(),
            direct.instance()
        )

    private val binding by viewBinding(ControllerImportWalletBinding::inflate)

    override fun bindView(modelFlow: Flow<M>): Flow<E> {
        modelFlow
            .map { it.loadingState }
            .distinctUntilChanged()
            .onEach { state ->
                val isLoading = state != M.LoadingState.IDLE
                // Disable navigation
                with(binding) {
                    scanButton.isEnabled = !isLoading
                    faqButton.isEnabled = !isLoading
                    closeButton.isEnabled = !isLoading

                    // Set loading visibility
                    scanButton.isGone = isLoading
                    progressBar.isVisible = isLoading
                    labelImportStatus.isVisible = isLoading
                }

                // Set loading message
                val messageId = when (state) {
                    M.LoadingState.ESTIMATING,
                    M.LoadingState.VALIDATING -> R.string.Import_checking
                    M.LoadingState.SUBMITTING -> R.string.Import_importing
                    else -> null
                }
                messageId?.let(binding.labelImportStatus::setText)
            }
            .launchIn(uiBindScope)

        return with(binding) {
            merge(
                closeButton.clicks().map { E.OnCloseClicked },
                faqButton.clicks().map { E.OnFaqClicked },
                scanButton.clicks().map { E.OnScanClicked }
            )
        }
    }

    override fun handleBack(): Boolean {
        return currentModel.isLoading
    }

    override fun onPasswordConfirmed(password: String) {
        E.OnPasswordEntered(password)
            .run(eventConsumer::accept)
    }

    override fun onPasswordCancelled() {
        E.OnImportCancel
            .run(eventConsumer::accept)
    }

    override fun onLinkScanned(link: Link) {
        if (link is Link.ImportWallet) {
            E.OnKeyScanned(link.privateKey, link.passwordProtected)
                .run(eventConsumer::accept)
        }
    }

    override fun onPositiveClicked(
        dialogId: String,
        controller: AlertDialogController,
        result: AlertDialogController.DialogInputResult
    ) {
        when (dialogId) {
            CONFIRM_IMPORT_DIALOG -> E.OnImportConfirm
            IMPORT_SUCCESS_DIALOG -> E.OnCloseClicked
            else -> null
        }?.run(eventConsumer::accept)
    }

    override fun onDismissed(
        dialogId: String,
        controller: AlertDialogController,
        result: AlertDialogController.DialogInputResult
    ) {
        onDismissOrNegative(dialogId)
    }

    override fun onNegativeClicked(
        dialogId: String,
        controller: AlertDialogController,
        result: AlertDialogController.DialogInputResult
    ) {
        onDismissOrNegative(dialogId)
    }

    private fun onDismissOrNegative(dialogId: String) {
        when (dialogId) {
            CONFIRM_IMPORT_DIALOG -> E.OnImportCancel
            IMPORT_SUCCESS_DIALOG -> E.OnCloseClicked
            else -> null
        }?.run(eventConsumer::accept)
    }

    override fun handleViewEffect(effect: ViewEffect) {
        if (effect is F.ShowPasswordInput) {
            router.pushController(RouterTransaction.with(PasswordController()))
        }
    }
}
