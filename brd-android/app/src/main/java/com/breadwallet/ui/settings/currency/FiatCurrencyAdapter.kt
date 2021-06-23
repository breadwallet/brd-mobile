/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 1/7/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings.currency

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.breadwallet.R
import com.breadwallet.databinding.CurrencyListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Currency

class FiatCurrencyAdapter(
    private val currenciesFlow: Flow<List<String>>,
    private val selectedCurrencyFlow: Flow<String>,
    private val sendChannel: SendChannel<DisplayCurrency.E>
) : RecyclerView.Adapter<FiatCurrencyAdapter.CurrencyViewHolder>() {

    private var currencies: List<String> = emptyList()
    private var selectedCurrencyCode: String = ""

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        currenciesFlow
            .onEach { currencies ->
                this.currencies = currencies
                notifyDataSetChanged()
            }
            .launchIn(CoroutineScope(Dispatchers.Main))

        selectedCurrencyFlow
            .onEach { currency ->
                this.selectedCurrencyCode = currency
                notifyDataSetChanged()
            }
            .launchIn(CoroutineScope(Dispatchers.Main))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.currency_list_item, parent, false)
        return CurrencyViewHolder(view)
    }

    override fun getItemCount() = currencies.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: CurrencyViewHolder, position: Int) {
        val currency = currencies[position]
        viewHolder.check.isVisible = currency.equals(selectedCurrencyCode, true)
        try {
            viewHolder.label.text = "$currency  (${Currency.getInstance(currency).symbol})"
        } catch (ignored: IllegalArgumentException) {
            viewHolder.label.text = currency
        }
        viewHolder.itemView.setOnClickListener {
            sendChannel.offer(DisplayCurrency.E.OnCurrencySelected(currencyCode = currency))
        }
    }

    class CurrencyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = CurrencyListItemBinding.bind(view)
        val label: TextView = binding.currencyItemText
        val check: ImageView = binding.currencyCheckmark
    }
}
