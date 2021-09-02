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
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.databinding.ControllerExchangeEmptyWalletsBinding

class EmptyWalletsController(args: Bundle? = null) : ExchangeController.ChildController(args) {

    private val binding by viewBinding(ControllerExchangeEmptyWalletsBinding::inflate)

    override fun ExchangeModel.render() = Unit

    override fun onAttach(view: View) {
        super.onAttach(view)

        binding.buttonClose.setOnClickListener {
            eventConsumer.accept(ExchangeEvent.OnCloseClicked(confirmed = true))
        }

        binding.buttonGoToBuy.setOnClickListener {
            eventConsumer.accept(ExchangeEvent.OnContinueClicked)
        }
    }
}
