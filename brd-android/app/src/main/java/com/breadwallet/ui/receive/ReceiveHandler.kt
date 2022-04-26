/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/15/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.receive

import android.content.Context
import com.breadwallet.breadbox.BreadBox
import com.breadwallet.breadbox.isBitcoin
import com.breadwallet.breadbox.toSanitizedString
import com.blockset.walletkit.AddressScheme
import com.breadwallet.repository.RatesRepository
import com.breadwallet.tools.manager.BRClipboardManager
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.ui.receive.ReceiveScreen.E
import com.breadwallet.ui.receive.ReceiveScreen.F
import drewcarlson.mobius.flow.subtypeEffectHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.invoke
import java.math.BigDecimal

private const val RATE_UPDATE_MS = 60_000L

fun createReceiveHandler(
    context: Context,
    breadBox: BreadBox
) = subtypeEffectHandler<F, E> {
    addConsumer<F.CopyAddressToClipboard> { effect ->
        Dispatchers.Main {
            BRClipboardManager.putClipboard(effect.address)
        }
        EventUtils.pushEvent(EventUtils.EVENT_RECEIVE_COPIED_ADDRESS)
    }

    addTransformer<F.LoadExchangeRate> { effects ->
        val rates = RatesRepository.getInstance(context)
        val fiatCode = BRSharedPrefs.getPreferredFiatIso()
        effects.transformLatest { effect ->
            while (true) {
                val fiatRate = rates.getFiatForCrypto(BigDecimal.ONE, effect.currencyCode, fiatCode)

                emit(E.OnExchangeRateUpdated(fiatRate ?: BigDecimal.ZERO))

                // TODO: Display out of date, invalid (0) rate, etc.
                delay(RATE_UPDATE_MS)
            }
        }
    }

    addTransformer<F.LoadWalletInfo> { effects ->
        effects.flatMapLatest { effect ->
            breadBox.wallet(effect.currencyCode).map { wallet ->
                val receiveAddress = if (wallet.currency.isBitcoin()) {
                    wallet.getTargetForScheme(
                        when (BRSharedPrefs.getIsSegwitEnabled()) {
                            true -> AddressScheme.BTC_SEGWIT
                            false -> AddressScheme.BTC_LEGACY
                        }
                    )
                } else {
                    wallet.target
                }
                E.OnWalletInfoLoaded(
                    walletName = wallet.currency.name,
                    address = receiveAddress.toString(),
                    sanitizedAddress = receiveAddress.toSanitizedString()
                )
            }
        }
    }
}
