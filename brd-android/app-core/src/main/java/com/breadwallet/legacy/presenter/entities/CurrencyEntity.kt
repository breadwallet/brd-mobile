/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 8/18/15.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.legacy.presenter.entities

import java.io.Serializable

data class CurrencyEntity(
    /** this currency code (USD, RUB) */
    var code: String,
    /** this currency name (Dollar) */
    var name: String,
    var rate: Float,
    /** this wallet's iso (BTC, BCH) */
    var iso: String
) : Serializable {

    companion object {
        const val serialVersionUID = 7526472295622776147L
        val TAG = CurrencyEntity::class.java.name
    }
}
