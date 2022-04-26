/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.support

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.SpannedString
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.getSpans
import com.brd.support.SupportModel
import com.breadwallet.R
import com.breadwallet.databinding.ListItemSupportArticleBodyBinding
import com.breadwallet.tools.html.CustomBulletSpan
import com.breadwallet.tools.html.LiTagHandler
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
                labelBody.text = getSpannedString(item)
                labelBody.movementMethod = LinkMovementMethod.getInstance()
            }
        }

        /** Added for better Bullet formatting for unordered lists of HTMl content */
        private fun getSpannedString(item: SupportArticleBodyItem): SpannedString {
            @Suppress("DEPRECATION")
            val htmlSpannable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(item.model.body, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(item.model.body, null, LiTagHandler())
            }

            return buildSpannedString {
                append(htmlSpannable)
                getSpans<BulletSpan>(0, length).forEach {
                    val start = getSpanStart(it)
                    val end = getSpanEnd(it)
                    removeSpan(it)
                    setSpan(
                        CustomBulletSpan(bulletRadius = 8, gapWidth = 32),
                        start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }

        override fun unbindView(item: SupportArticleBodyItem) = Unit
    }
}
