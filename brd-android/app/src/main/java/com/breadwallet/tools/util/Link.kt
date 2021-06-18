/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 11/24/202.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.util

import com.breadwallet.protocols.messageexchange.entities.PairingMetaData
import com.breadwallet.util.CurrencyCode
import dev.zacsweers.redacted.annotations.Redacted
import java.io.Serializable
import java.math.BigDecimal

sealed class Link {

    /**
     * A request to transfer a crypto via URL.
     * Loosely observes the following specs:
     *
     * https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki
     * https://github.com/bitcoin/bips/blob/master/bip-0072.mediawiki
     * https://github.com/ethereum/EIPs/blob/master/EIPS/eip-681.md
     */
    data class CryptoRequestUrl(
        val currencyCode: CurrencyCode,
        @Redacted val address: String? = null,
        val amount: BigDecimal? = null,
        @Redacted val label: String? = null,
        @Redacted val message: String? = null,
        @Redacted val reqParam: String? = null,
        @Redacted val rUrlParam: String? = null,
        @Redacted val destinationTag: String? = null,
    ) : Link(), Serializable

    data class WalletPairUrl(
        val pairingMetaData: PairingMetaData
    ) : Link()

    data class PlatformUrl(
        val url: String
    ) : Link()

    data class PlatformDebugUrl(
        val webBundle: String?,
        val webBundleUrl: String?
    ) : Link()

    data class ImportWallet(
        @Redacted val privateKey: String,
        val passwordProtected: Boolean,
        val gift: Boolean = false,
        val scanned: Boolean = false
    ) : Link()

    sealed class BreadUrl : Link() {
        object ScanQR : BreadUrl()
        object Address : BreadUrl()
        object AddressList : BreadUrl()
    }
}
