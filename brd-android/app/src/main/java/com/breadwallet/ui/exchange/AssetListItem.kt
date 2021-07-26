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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.brd.api.models.ExchangeCurrency
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.databinding.ExchangeAssetListItemBinding
import com.breadwallet.databinding.ExchangeCurrencyListItemBinding
import com.breadwallet.tools.util.TokenUtil
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.ModelAbstractItem
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

class AssetListItem(
    currency: ExchangeCurrency,
    val currentModel: () -> ExchangeModel,
) : ModelAbstractItem<ExchangeCurrency, AssetListItem.ViewHolder>(currency) {

    override val layoutRes: Int = R.layout.exchange_asset_list_item

    override val type: Int = R.id.currency_item

    override var identifier: Long = model.code.hashCode().toLong()

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(v: View) : FastAdapter.ViewHolder<AssetListItem>(v) {
        private val binding = ExchangeAssetListItemBinding.bind(v)
        override fun bindView(item: AssetListItem, payloads: List<Any>) = with(binding) {
            val currentModel = item.currentModel()
            imageItemValue.isVisible = item.isSelected
            val color = binding.root.resources.getColor(R.color.hydra_quaternary_background, itemView.context.theme)
            card.setCardBackgroundColor(if (item.isSelected) ColorStateList.valueOf(color) else null)
            labelCurrencyName.text = item.model.name
            labelCurrencyCode.text = item.model.code.toUpperCase(Locale.ROOT)

            if (currentModel.mode == ExchangeModel.Mode.TRADE) {
                val balance = currentModel.formattedCryptoBalances[item.model.code]
                labelCurrencyRate.text = balance?.takeIf { !it.startsWith("0 ") }
                labelCurrencyBalance.isVisible = false
            } else {
                labelCurrencyRate.apply {
                    val rate = currentModel.formattedFiatRates[item.model.code]
                    isGone = rate.isNullOrBlank()
                    text = rate
                }
                labelCurrencyBalance.apply {
                    val balance = currentModel.formattedCryptoBalances[item.model.code]
                    isGone = balance.isNullOrBlank() || balance.startsWith("0 ")
                    text = "Balance $balance"
                }
            }

            val currencyIcon = TokenUtil.getTokenIconPath(item.model.code, true)
            if (currencyIcon.isNullOrBlank()) {
                imageCurrencyIcon.isVisible = false
            } else {
                Picasso.get()
                    .load(File(currencyIcon))
                    .into(imageCurrencyIcon)
                imageCurrencyIcon.isVisible = true
            }
        }

        override fun unbindView(item: AssetListItem) = with(binding) {
            labelCurrencyName.text = null
            labelCurrencyCode.text = null
            labelCurrencyRate.text = null
            labelCurrencyBalance.text = null
            imageItemValue.isVisible = false
            labelCurrencyRate.isVisible = true
            labelCurrencyBalance.isVisible = true
            Picasso.get().cancelRequest(imageCurrencyIcon)
            imageCurrencyIcon.setImageDrawable(null)
        }
    }
}
