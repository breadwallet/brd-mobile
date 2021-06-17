/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.breadwallet.R
import com.breadwallet.databinding.SettingsListItemBinding

class SettingsAdapter(
    private val items: List<SettingsItem>,
    private val onClick: (SettingsOption) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SettingsViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.settings_list_item, viewGroup, false)
        return SettingsViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SettingsViewHolder, position: Int) {
        viewHolder.bindView(items[position])
        viewHolder.itemView.setOnClickListener { onClick(items[viewHolder.adapterPosition].option) }
    }

    override fun getItemCount() = items.size

    class SettingsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = SettingsListItemBinding.bind(view)

        fun bindView(item: SettingsItem) {
            binding.itemTitle.text = item.title
            item.iconResId?.let {
                binding.settingIcon.isVisible = true
                binding.settingIcon.setBackgroundResource(it)
            }
            binding.itemAddon.text = item.addOn
            binding.itemSubHeader.isGone = item.subHeader.isBlank()
            binding.itemSubHeader.text = item.subHeader
        }
    }
}
