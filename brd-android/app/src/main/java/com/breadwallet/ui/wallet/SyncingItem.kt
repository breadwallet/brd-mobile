/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 4/23/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.wallet

import android.view.View
import com.breadwallet.R
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class SyncingItem : AbstractItem<SyncingItem.ViewHolder>() {

    override val type: Int = R.id.syncing_item
    override val layoutRes: Int = R.layout.wallet_sync_progress_view
    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(
        v: View
    ) : FastAdapter.ViewHolder<SyncingItem>(v) {

        override fun bindView(item: SyncingItem, payloads: List<Any>) = Unit

        override fun unbindView(item: SyncingItem) = Unit
    }
}
