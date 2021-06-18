/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.util

import com.breadwallet.tools.util.bch
import com.breadwallet.tools.util.btc
import com.breadwallet.tools.util.eth
import com.breadwallet.tools.util.hbar
import com.breadwallet.tools.util.xtz

typealias CurrencyCode = String

fun CurrencyCode.isBitcoinLike(): Boolean = isBitcoin() || isBitcoinCash()
fun CurrencyCode.isBitcoin(): Boolean = equals(btc, true)
fun CurrencyCode.isBitcoinCash(): Boolean = equals(bch, true)
fun CurrencyCode.isEthereum(): Boolean = equals(eth, true)
fun CurrencyCode.isBrd(): Boolean = equals("brd", true)
fun CurrencyCode.isRipple(): Boolean = equals("xrp", true)
fun CurrencyCode.isHedera(): Boolean = equals(hbar, true)
fun CurrencyCode.isTezos(): Boolean = equals(xtz, true)
fun CurrencyCode.isErc20(): Boolean {
    return !isBitcoin() && !isBitcoinCash() && !isEthereum() && !isRipple() && !isHedera() && !isTezos()
}

