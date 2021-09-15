/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/13/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.send

import com.breadwallet.ui.send.SendSheet.F
import com.breadwallet.ui.send.SendSheet.M
import com.spotify.mobius.First
import com.spotify.mobius.Init

object SendSheetInit : Init<M, F> {
    override fun init(model: M): First<M, F> {
        val effects = mutableSetOf<F>()

        if (model.targetAddress.isNotBlank()) {
            effects.add(
                F.ValidateAddress(
                    model.currencyCode,
                    model.targetAddress
                )
            )
        }

        var isPaymentProtocolRequest = false
        if (!model.cryptoRequestUrl?.rUrlParam.isNullOrBlank()) {
            effects.add(F.PaymentProtocol.LoadPaymentData(model.cryptoRequestUrl!!))
            isPaymentProtocolRequest = true
        }

        return First.first(
            model.copy(isFetchingPayment = isPaymentProtocolRequest),
            effects + setOf(
                F.LoadBalance(model.currencyCode),
                F.LoadExchangeRate(model.currencyCode, model.fiatCode),
                F.LoadAuthenticationSettings,
                F.GetTransferFields(model.currencyCode, model.targetAddress)
            )
        )
    }
}
