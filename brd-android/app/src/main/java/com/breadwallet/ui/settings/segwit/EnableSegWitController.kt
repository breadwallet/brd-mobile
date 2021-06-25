/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/05/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import android.os.Bundle
import androidx.core.view.isVisible
import com.breadwallet.databinding.ControllerEnableSegwitBinding
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.flowbind.clicks
import com.breadwallet.ui.settings.segwit.EnableSegWit.E
import com.breadwallet.ui.settings.segwit.EnableSegWit.F
import com.breadwallet.ui.settings.segwit.EnableSegWit.M
import drewcarlson.mobius.flow.FlowTransformer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.kodein.di.direct
import org.kodein.di.erased.instance

class EnableSegWitController(
    args: Bundle? = null
) : BaseMobiusController<M, E, F>(args) {

    override val defaultModel = M()
    override val update = EnableSegWitUpdate
    override val flowEffectHandler: FlowTransformer<F, E>
        get() = createSegWitHandler(
            checkNotNull(applicationContext),
            direct.instance()
        )

    private val binding by viewBinding(ControllerEnableSegwitBinding::inflate)

    override fun bindView(modelFlow: Flow<M>): Flow<E> {
        return with(binding) {
            merge(
                enableButton.clicks().map { E.OnEnableClick },
                backButton.clicks().map { E.OnBackClicked },
                continueButton.clicks().map { E.OnContinueClicked },
                cancelButton.clicks().map { E.OnCancelClicked },
                doneButton.clicks().map { E.OnDoneClicked }
            )
        }
    }

    override fun M.render() {
        with(binding) {
            ifChanged(M::state) {
                confirmChoiceLayout.isVisible = state == M.State.CONFIRMATION
                enableButton.isVisible = state == M.State.ENABLE
                doneButton.isVisible = state == M.State.DONE
                confirmationLayout.isVisible = state == M.State.DONE
            }
        }
    }
}
