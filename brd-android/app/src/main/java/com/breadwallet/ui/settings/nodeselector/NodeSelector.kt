/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 11/05/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.nodeselector

import com.breadwallet.crypto.WalletManagerState
import com.breadwallet.ui.ViewEffect
import dev.zacsweers.redacted.annotations.Redacted

object NodeSelector {

    enum class Mode { AUTOMATIC, MANUAL }

    data class M(
        val mode: Mode? = null,
        @Redacted val currentNode: String = "",
        val connected: Boolean = false
    ) {

        companion object {
            fun createDefault() = M()
        }
    }

    sealed class E {
        object OnSwitchButtonClicked : E()

        data class OnConnectionStateUpdated(
            val state: WalletManagerState
        ) : E()

        data class OnConnectionInfoLoaded(
            val mode: Mode,
            @Redacted val node: String = ""
        ) : E()

        data class SetCustomNode(@Redacted val node: String) : E()
    }

    sealed class F {
        object ShowNodeDialog : F(), ViewEffect
        object LoadConnectionInfo : F()
        object SetToAutomatic : F()
        data class SetCustomNode(@Redacted val node: String) : F()
    }
}
