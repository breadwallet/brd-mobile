/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/5/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.bakerapi

import kotlinx.coroutines.CoroutineScope

// Patch for https://github.com/Kotlin/kotlinx.coroutines/issues/770
actual fun runBlocking(block: suspend CoroutineScope.() -> Unit) = Unit
