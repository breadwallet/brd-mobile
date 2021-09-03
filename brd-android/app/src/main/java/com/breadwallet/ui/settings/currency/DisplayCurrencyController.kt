/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 1/7/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.currency

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.breadwallet.databinding.ControllerDisplayCurrencyBinding
import com.breadwallet.ui.BaseMobiusController
import com.breadwallet.ui.flowbind.clicks
import com.breadwallet.ui.settings.currency.DisplayCurrency.E
import com.breadwallet.ui.settings.currency.DisplayCurrency.F
import com.breadwallet.ui.settings.currency.DisplayCurrency.M
import drewcarlson.mobius.flow.FlowTransformer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.kodein.di.direct
import org.kodein.di.instance

class DisplayCurrencyController(
    args: Bundle? = null
) : BaseMobiusController<M, E, F>(args) {

    override val defaultModel = M.createDefault()
    override val init = DisplayCurrencyInit
    override val update = CurrencyUpdate
    override val flowEffectHandler: FlowTransformer<F, E>
        get() = createDisplayCurrencyHandler(
            checkNotNull(applicationContext),
            direct.instance()
        )

    private val binding by viewBinding(ControllerDisplayCurrencyBinding::inflate)

    override fun bindView(modelFlow: Flow<M>): Flow<E> {
        return with(binding) {
            currencyList.layoutManager = LinearLayoutManager(checkNotNull(activity))
            merge(
                backButton.clicks().map { E.OnBackClicked },
                faqButton.clicks().map { E.OnFaqClicked },
                bindCurrencyList(modelFlow)
            )
        }
    }

    private fun bindCurrencyList(modelFlow: Flow<M>) = callbackFlow<E> {
        binding.currencyList.adapter = FiatCurrencyAdapter(
            modelFlow
                .map { model -> model.currencies }
                .distinctUntilChanged(),
            modelFlow
                .map { model -> model.selectedCurrency }
                .distinctUntilChanged(),
            sendChannel = channel
        )
        awaitClose { binding.currencyList.adapter = null }
    }
}
