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
import com.brd.support.*
import com.breadwallet.R
import com.breadwallet.databinding.ListItemSupportArticleBinding
import com.mikepenz.fastadapter.*
import com.mikepenz.fastadapter.items.*

class SupportSectionItem(
    model: SupportModel.Section
) : ModelAbstractItem<SupportModel.Section, SupportSectionItem.ViewHolder>(model) {

    override val layoutRes: Int = R.layout.list_item_support_article

    override val type: Int = R.id.support_section_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<SupportSectionItem>(view) {

        override fun bindView(item: SupportSectionItem, payloads: List<Any>) {
            with(ListItemSupportArticleBinding.bind(itemView)) {
                labelTitle.text = item.model.title
            }
        }

        override fun unbindView(item: SupportSectionItem) {
        }
    }
}
