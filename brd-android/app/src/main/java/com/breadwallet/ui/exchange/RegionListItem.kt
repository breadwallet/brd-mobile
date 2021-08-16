/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.content.res.ColorStateList
import android.view.View
import androidx.core.view.isVisible
import com.brd.api.models.ExchangeCountry
import com.brd.api.models.ExchangeRegion
import com.breadwallet.R
import com.breadwallet.databinding.CountryListItemBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.ModelAbstractItem

class RegionListItem(
    region: ExchangeRegion
) : ModelAbstractItem<ExchangeRegion, RegionListItem.ViewHolder>(region) {

    override val layoutRes: Int = R.layout.country_list_item

    override val type: Int = R.id.region_item

    override var identifier: Long = model.code.hashCode().toLong()

    override fun getViewHolder(v: View) = ViewHolder(v)

    inner class ViewHolder(v: View) : FastAdapter.ViewHolder<RegionListItem>(v) {
        private val binding = CountryListItemBinding.bind(v)

        override fun bindView(item: RegionListItem, payloads: List<Any>) = with(binding) {
            labelItemValue.text = item.model.name
            imageItemValue.isVisible = item.isSelected
            val color = binding.root.resources.getColor(R.color.hydra_quaternary_background, itemView.context.theme)
            card.setCardBackgroundColor(if (item.isSelected) ColorStateList.valueOf(color) else null)
        }

        override fun unbindView(item: RegionListItem) = with(binding) {
            labelItemValue.text = null
            imageItemValue.isVisible = false
        }
    }
}
