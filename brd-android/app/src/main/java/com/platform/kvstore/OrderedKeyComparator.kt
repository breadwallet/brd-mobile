/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 12/18/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.platform.kvstore

import com.platform.sqlite.KVItem

class OrderedKeyComparator(private val orderedKeys: List<String>) : Comparator<KVItem> {
    override fun compare(o1: KVItem?, o2: KVItem?): Int =
        when {
            o1 == null && o2 == null -> 0
            o1 == null -> 1
            o2 == null -> -1
            orderedKeys.contains(o1.key) && orderedKeys.contains((o2.key)) ->
                orderedKeys.indexOf(o1.key) - orderedKeys.indexOf(o2.key)
            orderedKeys.contains(o1.key) -> -1
            orderedKeys.contains(o2.key) -> 1
            else -> o1.key.compareTo(o2.key)
        }
}
