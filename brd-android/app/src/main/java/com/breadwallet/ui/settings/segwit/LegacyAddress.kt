/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/05/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.segwit

import com.breadwallet.ui.navigation.NavigationEffect
import com.breadwallet.ui.navigation.NavigationTarget
import dev.zacsweers.redacted.annotations.Redacted

object LegacyAddress {

    data class M(
        /** The network compatible address for transactions. */
        @Redacted val receiveAddress: String = "",
        /** The address without network specific decoration. */
        @Redacted val sanitizedAddress: String = "",
        /** The name of the Wallet's currency. */
        val walletName: String = ""
    )

    sealed class E {
        object OnShareClicked : E()
        object OnAddressClicked : E()
        object OnCloseClicked : E()

        data class OnAddressUpdated(
            @Redacted val receiveAddress: String,
            @Redacted val sanitizedAddress: String
        ) : E()

        data class OnWalletNameUpdated(
            val name: String
        ) : E()
    }

    sealed class F {
        object LoadAddress : F()
        object GoBack : F(), NavigationEffect {
            override val navigationTarget = NavigationTarget.Back
        }

        data class CopyAddressToClipboard(@Redacted val address: String) : F()

        data class ShareAddress(@Redacted val address: String, val walletName: String) : F()
    }
}
