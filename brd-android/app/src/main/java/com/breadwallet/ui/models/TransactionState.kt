/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 9/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.models

import com.breadwallet.crypto.TransferState

enum class TransactionState {
    CONFIRMED, CONFIRMING, FAILED, DELETED;

    companion object {
        fun valueOf(transferState: TransferState): TransactionState {
            return when (transferState.type) {
                TransferState.Type.INCLUDED -> CONFIRMED
                TransferState.Type.FAILED -> FAILED
                TransferState.Type.CREATED,
                TransferState.Type.SIGNED,
                TransferState.Type.SUBMITTED,
                TransferState.Type.PENDING -> CONFIRMING
                TransferState.Type.DELETED -> DELETED
            }
        }
    }
}
