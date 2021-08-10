/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.support

import android.view.*
import com.breadwallet.R
import com.breadwallet.databinding.ListItemSupportTitleBinding
import com.mikepenz.fastadapter.*
import com.mikepenz.fastadapter.items.*

class SupportTitleItem(
    model: String
) : ModelAbstractItem<String, SupportTitleItem.ViewHolder>(model) {

    override val layoutRes: Int = R.layout.list_item_support_title

    override val type: Int = R.id.support_title_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<SupportTitleItem>(view) {

        override fun bindView(item: SupportTitleItem, payloads: List<Any>) {
            with(ListItemSupportTitleBinding.bind(itemView)) {
                labelTitle.text = item.model
            }
        }

        override fun unbindView(item: SupportTitleItem) {
        }
    }
}
