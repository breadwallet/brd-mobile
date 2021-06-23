/**
 * BreadWallet
 *
 * Created by Alan Hill <alan.hill@breadwallet.com> on 6/5/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.model

import java.util.Date

data class PriceDataPoint(val time: Date, val closePrice: Double)
