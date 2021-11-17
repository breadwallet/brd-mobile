/*
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 11/03/2021
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.util

import com.brd.api.models.CurrencyMethod
import com.breadwallet.R

val CurrencyMethod.methodStringRes: Int
    get() {
        return when (this) {
            is CurrencyMethod.Ach -> R.string.Exchange_viaACH
            is CurrencyMethod.Card -> R.string.Exchange_viaCard
            is CurrencyMethod.Crypto -> R.string.Exchange_viaCrypto
            is CurrencyMethod.Sepa -> R.string.Exchange_viaSEPA
        }
    }
