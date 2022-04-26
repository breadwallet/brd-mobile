/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/05/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import android.content.Context
import com.breadwallet.breadbox.BreadBox
import com.blockset.walletkit.AddressScheme
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.ui.settings.segwit.EnableSegWit.E
import com.breadwallet.ui.settings.segwit.EnableSegWit.F
import com.breadwallet.util.isBitcoin
import com.breadwallet.util.usermetrics.UserMetricsUtil
import drewcarlson.mobius.flow.subtypeEffectHandler
import kotlinx.coroutines.flow.first

fun createSegWitHandler(
    context: Context,
    breadBox: BreadBox
) = subtypeEffectHandler<F, E> {
    addAction<F.EnableSegWit> {
        BRSharedPrefs.putIsSegwitEnabled(true)
        UserMetricsUtil.logSegwitEvent(context, UserMetricsUtil.ENABLE_SEG_WIT)
        breadBox.system()
            .first()
            .walletManagers
            .find { it.currency.code.isBitcoin() }
            ?.addressScheme = AddressScheme.BTC_SEGWIT
    }
}
