/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/24/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
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