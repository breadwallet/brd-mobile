/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 3/25/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.home

import android.view.View
import com.breadwallet.R
import com.breadwallet.databinding.AddWalletsItemBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.drag.IDraggable
import com.mikepenz.fastadapter.items.AbstractItem

class AddWalletItem : AbstractItem<AddWalletItem.ViewHolder>(), IDraggable {

    override val type: Int = R.id.add_wallet_item
    override val layoutRes: Int = R.layout.add_wallets_item
    override var identifier: Long = 0

    override val isDraggable: Boolean = false

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(
        v: View
    ) : FastAdapter.ViewHolder<AddWalletItem>(v) {

        init {
            val res = v.resources
            AddWalletsItemBinding.bind(v)
                .addWallets
                .text = "+ ${res.getString(R.string.TokenList_addTitle)}"
        }

        override fun bindView(
            item: AddWalletItem,
            payloads: List<Any>
        ) = Unit

        override fun unbindView(item: AddWalletItem) = Unit
    }
}
