/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/25/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.fingerprint

import com.breadwallet.databinding.ControllerFingerprintSettingsBinding
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.flowbind.checked
import com.breadwallet.ui.flowbind.clicks
import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.E
import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.F
import com.breadwallet.ui.settings.fingerprint.FingerprintSettings.M
import drewcarlson.mobius.flow.FlowTransformer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

class FingerprintSettingsController : BaseMobiusController<M, E, F>() {

    override val defaultModel = M()
    override val update = FingerprintSettingsUpdate
    override val init = FingerprintSettingsInit
    override val flowEffectHandler: FlowTransformer<F, E>
        get() = createFingerprintSettingsHandler()

    private val binding by viewBinding(ControllerFingerprintSettingsBinding::inflate)

    override fun bindView(modelFlow: Flow<M>): Flow<E> {
        return with(binding) {
            modelFlow.map { it.unlockApp }
                .onEach { switchUnlockApp.isChecked = it }
                .launchIn(uiBindScope)

            modelFlow.map { it.sendMoney }
                .onEach { switchSendMoney.isChecked = it }
                .launchIn(uiBindScope)

            modelFlow.map { it.sendMoneyEnable }
                .onEach { switchSendMoney.isEnabled = it }
                .launchIn(uiBindScope)

            merge(
                faqBtn.clicks().map { E.OnFaqClicked },
                backBtn.clicks().map { E.OnBackClicked },
                switchSendMoney.checked().map { E.OnSendMoneyChanged(it) },
                switchUnlockApp.checked().map { E.OnAppUnlockChanged(it) }
            )
        }
    }
}
