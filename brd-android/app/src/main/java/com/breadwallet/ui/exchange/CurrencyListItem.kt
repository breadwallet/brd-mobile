/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.graphics.Color
import android.view.View
import androidx.core.view.isVisible
import com.brd.api.models.ExchangeCurrency
import com.breadwallet.R
import com.breadwallet.databinding.ExchangeCurrencyListItemBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.ModelAbstractItem

class CurrencyListItem(
    currency: ExchangeCurrency,
) : ModelAbstractItem<ExchangeCurrency, CurrencyListItem.ViewHolder>(currency) {

    override val layoutRes: Int = R.layout.exchange_currency_list_item

    override val type: Int = R.id.currency_item

    override var identifier: Long = model.code.hashCode().toLong()

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(v: View) : FastAdapter.ViewHolder<CurrencyListItem>(v) {
        private val binding = ExchangeCurrencyListItemBinding.bind(v)
        override fun bindView(item: CurrencyListItem, payloads: List<Any>) = with(binding) {
            imageItemValue.isVisible = item.isSelected
            val color = binding.root.resources.getColor(R.color.hydra_quaternary_background, itemView.context.theme)
            card.setCardBackgroundColor(if (item.isSelected) color else Color.TRANSPARENT)
            val (code, name) = item.model.selectedFiatCurrencyName().split("-")
            labelCurrencyCode.text = code
            labelCurrencyName.text = name
        }

        override fun unbindView(item: CurrencyListItem) = with(binding) {
            labelCurrencyName.text = null
            labelCurrencyCode.text = null
            imageItemValue.isVisible = false
        }
    }
}
