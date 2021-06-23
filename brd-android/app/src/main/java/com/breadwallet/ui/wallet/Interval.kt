/**
 * BreadWallet
 *
 * Created by Alan Hill on <alan.hill@breadwallet.com> 6/8/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.wallet

enum class Interval(val days: Int, val keepEvery: Int) {
    ONE_DAY(1, 6),
    ONE_WEEK(7, 0),
    ONE_MONTH(30, 2),
    THREE_MONTHS(90, 8),
    ONE_YEAR(365, 1),
    THREE_YEARS(1095, 5)
}
