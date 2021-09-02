/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.support

import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import com.brd.support.SupportModel
import com.breadwallet.R
import com.breadwallet.databinding.ListItemSupportArticleBodyBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.ModelAbstractItem

class SupportArticleBodyItem(
    model: SupportModel.Article
) : ModelAbstractItem<SupportModel.Article, SupportArticleBodyItem.ViewHolder>(model) {

    override val layoutRes: Int = R.layout.list_item_support_article_body

    override val type: Int = R.id.support_article_body_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<SupportArticleBodyItem>(view) {

        override fun bindView(item: SupportArticleBodyItem, payloads: List<Any>) {
            with(ListItemSupportArticleBodyBinding.bind(itemView)) {
                labelBody.text = HtmlCompat.fromHtml(item.model.body, HtmlCompat.FROM_HTML_MODE_COMPACT)
                labelBody.movementMethod = LinkMovementMethod.getInstance()
            }
        }

        override fun unbindView(item: SupportArticleBodyItem) = Unit
    }
}
