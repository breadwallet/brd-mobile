/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/05/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.checkSelfPermission
import com.bluelinelabs.conductor.Controller
import com.breadwallet.breadbox.BreadBox
import com.breadwallet.breadbox.toSanitizedString
import com.breadwallet.crypto.AddressScheme
import com.breadwallet.legacy.presenter.entities.CryptoRequest
import com.breadwallet.mobius.bindConsumerIn
import com.breadwallet.tools.manager.BRClipboardManager
import com.breadwallet.tools.qrcode.QRUtils
import com.breadwallet.tools.util.btc
import com.breadwallet.ui.settings.segwit.LegacyAddress.E
import com.breadwallet.ui.settings.segwit.LegacyAddress.F
import com.breadwallet.util.CryptoUriParser
import com.breadwallet.util.errorHandler
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LegacyAddressHandler(
    private val output: Consumer<E>,
    private val breadBox: BreadBox,
    private val cryptoUriParser: CryptoUriParser,
    private val controller: Controller,
    private val showAddressCopiedAnimation: () -> Unit
) : Connection<F>, CoroutineScope {

    override val coroutineContext = SupervisorJob() + Dispatchers.Default + errorHandler()

    init {
        breadBox.wallet(btc)
            .map { wallet ->
                wallet.getTargetForScheme(AddressScheme.BTC_LEGACY)
            }
            .distinctUntilChanged()
            .map { E.OnAddressUpdated(it.toString(), it.toSanitizedString()) }
            .bindConsumerIn(output, this)

        breadBox.wallet(btc)
            .map { it.currency.name }
            .distinctUntilChanged()
            .map { E.OnWalletNameUpdated(it) }
            .bindConsumerIn(output, this)
    }

    override fun accept(effect: F) {
        when (effect) {
            is F.CopyAddressToClipboard -> {
                launch(Dispatchers.Main) {
                    BRClipboardManager.putClipboard(effect.address)
                    showAddressCopiedAnimation()
                }
            }
            is F.ShareAddress -> {
                launch(Dispatchers.Main) {
                    val context = checkNotNull(controller.applicationContext)
                    val writePerm = checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (writePerm == PackageManager.PERMISSION_GRANTED) {
                        val cryptoRequest = CryptoRequest.Builder()
                            .setAddress(effect.address)
                            .build()
                        cryptoUriParser.createUrl(btc, cryptoRequest)
                            ?.let { cryptoUri ->
                                QRUtils.sendShareIntent(
                                    context,
                                    cryptoUri.toString(),
                                    effect.address,
                                    effect.walletName
                                )
                            }
                            ?.run(controller::startActivity)
                    } else {
                        controller.requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            QRUtils.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_ID
                        )
                    }
                }
            }
        }
    }

    override fun dispose() {
        coroutineContext.cancel()
    }
}
