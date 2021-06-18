/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/1/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.legacy.presenter.entities

import java.io.Serializable

data class GenericTransactionMetaData(
    val targetAddress: String,
    val amount: String,
    val amountUnit: Unit,
    val gasPrice: Long = 0,
    val gasPriceUnit: Unit,
    val gasLimit: Long = 0,
    val data: String
) : Serializable