/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 5/05/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.wallet

import android.view.View
import com.breadwallet.R
import com.breadwallet.databinding.StakingViewBinding
import com.breadwallet.tools.util.TokenUtil
import com.breadwallet.util.CurrencyCode
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.squareup.picasso.Picasso
import java.io.File

class StakingItem(
    val currencyCode: CurrencyCode
) : AbstractItem<StakingItem.ViewHolder>() {

    override val type: Int = R.id.staking_item
    override val layoutRes: Int = R.layout.staking_view
    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(v: View) : FastAdapter.ViewHolder<StakingItem>(v) {

        override fun bindView(item: StakingItem, payloads: List<Any>) {
            with(StakingViewBinding.bind(itemView)) {
                val tokenIconPath = TokenUtil.getTokenIconPath(item.currencyCode, true)

                if (!tokenIconPath.isNullOrBlank()) {
                    val iconFile = File(tokenIconPath)
                    Picasso.get().load(iconFile).into(stakingTokenIcon)
                }
            }
        }

        override fun unbindView(item: StakingItem) = Unit
    }
}
