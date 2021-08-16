/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.databinding.OfferListItemBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.ModelAbstractItem
import com.squareup.picasso.Picasso


class OfferListItem(
    offerDetails: ExchangeModel.OfferDetails
) : ModelAbstractItem<ExchangeModel.OfferDetails, OfferListItem.ViewHolder>(offerDetails) {

    override val layoutRes: Int = R.layout.offer_list_item

    override val type: Int = R.id.offer_item

    override var identifier: Long = model.offer.hashCode().toLong()

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(
        v: View
    ) : FastAdapter.ViewHolder<OfferListItem>(v) {
        val binding = OfferListItemBinding.bind(v)

        override fun bindView(item: OfferListItem, payloads: List<Any>) {
            binding.bindToModel(item.model)
            binding.iconEnabled.isVisible = item.isSelected
        }

        override fun unbindView(item: OfferListItem) = with(binding) {
            labelFeeValue.text = null
            labelRateValue.text = null
            labelProviderName.text = null
            labelOutputAmount.text = null
            labelOutputAmountAlt.text = null
            binding.iconEnabled.isVisible = false
            Picasso.get().cancelRequest(imageProviderLogo)
        }
    }
}


fun OfferListItemBinding.bindToModel(model: ExchangeModel.OfferDetails) {
    when (model) {
        is ExchangeModel.OfferDetails.ValidOffer -> {
            labelSubtitle.isVisible = true
            labelSubtitle.text = model.offer.deliveryEstimate
            groupValid.isVisible = true
            buttonContinue.isVisible = false
            labelRateValue.text = model.formattedSourceRate
            labelFeeValue.text = model.formattedSourceFees

            labelOutputAmount.text = model.formattedQuoteTotal
            labelOutputAmountAlt.text = model.formattedSourceTotal
        }
        is ExchangeModel.OfferDetails.InvalidOffer -> {
            groupValid.isVisible = false
            buttonContinue.isVisible = true
            buttonContinue.text = when {
                !model.formattedMinSourceAmount.isNullOrBlank() -> {
                    root.context.getString(
                        R.string.Exchange_CTA_setMin,
                        model.formattedMinSourceAmount
                    )
                }
                !model.formattedMaxSourceAmount.isNullOrBlank() -> {
                    root.context.getString(
                        R.string.Exchange_CTA_setMax,
                        model.formattedMaxSourceAmount
                    )
                }
                else -> ""
            }
        }
    }
    labelProviderName.text = model.offer.provider.name

    when (model.offer.provider.slug.removeSuffix("-test")) {
        "moonpay" -> imageProviderLogo.setImageResource(R.drawable.ic_provider_moonpay)
        "wyre" -> imageProviderLogo.setImageResource(R.drawable.ic_provider_wyre)
        else -> {
            if (model.offer.provider.logoUrl.isNullOrBlank()) {
                imageProviderLogo.setImageDrawable(null)
            } else {
                Picasso.get().load(model.offer.provider.logoUrl).into(imageProviderLogo)
            }
        }
    }
}
