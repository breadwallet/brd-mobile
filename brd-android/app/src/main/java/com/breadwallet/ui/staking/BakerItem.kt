/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/24/21.
 * Copyright (c) 2021 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.breadwallet.ui.staking

import android.view.View
import com.brd.bakerapi.models.Baker
import com.breadwallet.R
import com.breadwallet.databinding.BakerListItemBinding
import com.breadwallet.databinding.BakerViewBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.squareup.picasso.Picasso
import java.text.NumberFormat

class BakerItem (
    val baker: Baker
) : AbstractItem<BakerItem.ViewHolder>() {

    override val type: Int = R.id.baker_item
    override val layoutRes: Int = R.layout.baker_list_item
    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(v: View) : FastAdapter.ViewHolder<BakerItem>(v) {

        override fun bindView(item: BakerItem, payloads: List<Any>) {
            with(BakerListItemBinding.bind(itemView).baker) {
                val formatter =  NumberFormat.getPercentInstance()
                name.text = item.baker.name
                feePct.text = itemView.context.getString(
                    R.string.Staking_feePct,
                    "${formatter.format(item.baker.fee)}"
                )
                formatter.maximumFractionDigits = 3
                formatter.minimumFractionDigits = 3
                roiPct.text = "${formatter.format(item.baker.estimatedRoi)}"
                Picasso.get().load(item.baker.logo).into(bakerTokenIcon)
            }
        }

        override fun unbindView(item: BakerItem) = Unit
    }
}