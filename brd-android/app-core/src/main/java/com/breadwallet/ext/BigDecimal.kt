/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/23/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ext

import java.math.BigDecimal

fun BigDecimal.isPositive() = compareTo(BigDecimal.ZERO) > 0
fun BigDecimal.isZero() = compareTo(BigDecimal.ZERO) == 0
fun BigDecimal.isNegative() = compareTo(BigDecimal.ZERO) < 0
