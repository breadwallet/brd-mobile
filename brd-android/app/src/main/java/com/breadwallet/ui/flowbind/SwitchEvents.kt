/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 12/6/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.flowbind

import android.widget.Switch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun Switch.checked(): Flow<Boolean> =
    callbackFlow {
        setOnCheckedChangeListener { _, isChecked ->
            offer(isChecked)
        }
        awaitClose {
            setOnCheckedChangeListener(null)
        }
    }
