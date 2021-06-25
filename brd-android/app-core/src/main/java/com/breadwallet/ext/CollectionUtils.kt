/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/13/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ext

import java.util.Collections

/** Returns a new [List] with the new [value] stored at [index]. */
fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    toMutableList().apply { set(index, value) }

fun <T> List<T>.swap(fromPosition: Int, toPosition: Int) =
    Collections.swap(this, fromPosition, toPosition)
