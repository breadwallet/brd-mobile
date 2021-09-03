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
import com.breadwallet.R
import com.breadwallet.databinding.CountryListItemBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.ModelAbstractItem
import java.util.Locale

class CountryListItem(
    country: ExchangeCountry
) : ModelAbstractItem<ExchangeCountry, CountryListItem.ViewHolder>(country) {

    override val layoutRes: Int = R.layout.country_list_item

    override val type: Int = R.id.country_item

    override var identifier: Long = model.code.hashCode().toLong()

    override fun getViewHolder(v: View) = ViewHolder(v)

    inner class ViewHolder(
        v: View
    ) : FastAdapter.ViewHolder<CountryListItem>(v) {
        private val binding = CountryListItemBinding.bind(v)

        override fun bindView(item: CountryListItem, payloads: List<Any>) = with(binding) {
            labelItemFlag.text = countryCodeToFlag(item.model.code)
            labelItemValue.text = item.model.name
            imageItemValue.isVisible = item.isSelected
            val color = binding.root.resources.getColor(R.color.hydra_quaternary_background, itemView.context.theme)
            card.setCardBackgroundColor(if (item.isSelected) ColorStateList.valueOf(color) else null)
        }

        override fun unbindView(item: CountryListItem) = with(binding) {
            labelItemValue.text = null
            imageItemValue.isVisible = false
        }
    }
}

fun countryCodeToFlag(countryCode: String): String {
    val offset = 0x1F1A5
    return countryCode.toUpperCase(Locale.ROOT).run {
        String(
            intArrayOf(
                codePointAt(0) + offset,
                codePointAt(1) + offset,
            ),
            offset = 0,
            length = 2
        )
    }
}
