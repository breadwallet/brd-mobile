/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 5/31/2019.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.util

/**
 * Returns a list containing only elements matching the given [predicate]
 * where [predicate] takes only the first ([L]) parameter of a pair.
 */
inline fun <L, R> Iterable<Pair<L, R>>.filterLeft(
        crossinline predicate: (L) -> Boolean
) = filter { predicate(it.first) }

/**
 * Returns a list containing only elements matching the given [predicate]
 * where [predicate] takes only the second ([R]) parameter of a pair.
 */
inline fun <L, R> Iterable<Pair<L, R>>.filterRight(
        crossinline predicate: (R) -> Boolean
) = filter { predicate(it.second) }

/**
 * Returns a list containing only the first ([L]) values of a pair.
 */
fun <L, R> Iterable<Pair<L, R>>.mapLeft() = map { it.first }

/**
 * Returns a list containing only the second ([R]) values of a pair.
 */
fun <L, R> Iterable<Pair<L, R>>.mapRight() = map { it.second }
