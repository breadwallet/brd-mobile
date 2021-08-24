/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.exchange

import android.animation.LayoutTransition
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brd.api.models.ExchangeCurrency
import com.brd.exchange.ExchangeEvent
import com.brd.exchange.ExchangeModel
import com.breadwallet.R
import com.breadwallet.databinding.ControllerExchangePickerBinding
import com.breadwallet.tools.recyclerview.MarginItemDecoration
import com.breadwallet.tools.util.TokenUtil
import com.breadwallet.tools.util.Utils
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ModelAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.select.getSelectExtension
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch

class PickerController(args: Bundle? = null) : ExchangeController.ChildController(args) {

    enum class SelectionType {
        COUNTRY, REGION, CURRENCY, ASSET, OFFER,
    }

    constructor(target: SelectionType) : this(
        bundleOf(SelectionType::class.java.simpleName to target)
    )

    val selectionType: SelectionType = arg(SelectionType::class.java.simpleName)

    private val binding by viewBinding(ControllerExchangePickerBinding::inflate)

    private val regionAdapter = ModelAdapter(::RegionListItem)
    private val currencyAdapter = ModelAdapter(::CurrencyListItem)
    private val assetAdapter =
        ModelAdapter<ExchangeCurrency, AssetListItem> { AssetListItem(it) { currentModel } }
    private val countriesAdapter = ModelAdapter(::CountryListItem)
    private val offersAdapter = ModelAdapter(::OfferListItem)
    private val fastAdapter = FastAdapter.with(
        listOf(countriesAdapter, assetAdapter, currencyAdapter, regionAdapter, offersAdapter)
    )

    init {
        countriesAdapter.itemFilter.filterPredicate = { item, constraint ->
            constraint.isNullOrBlank() ||
                item.model.name.contains(constraint, true) ||
                item.model.code.contains(constraint, true)
        }
        assetAdapter.itemFilter.filterPredicate = { item, constraint ->
            constraint.isNullOrBlank() ||
                item.model.name.contains(constraint, true) ||
                item.model.code.contains(constraint, true)
        }
        currencyAdapter.itemFilter.filterPredicate = { item, constraint ->
            constraint.isNullOrBlank() ||
                item.model.name.contains(constraint, true) ||
                item.model.code.contains(constraint, true)
        }
        regionAdapter.itemFilter.filterPredicate = { item, constraint ->
            constraint.isNullOrBlank() ||
                item.model.name.contains(constraint, true) ||
                item.model.code.contains(constraint, true)
        }
    }

    override fun onDetach(view: View) {
        Utils.hideKeyboard(activity)
        super.onDetach(view)
    }

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        with(binding) {
            buttonBack.setOnClickListener { eventConsumer.accept(ExchangeEvent.OnBackClicked) }
            toolbar.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            layoutSearch.isVisible = selectionType != SelectionType.OFFER
            val displayAssetToggle =
                selectionType == SelectionType.ASSET && currentModel.mode == ExchangeModel.Mode.TRADE
            title.isVisible = !displayAssetToggle
            tradeToggleGroup.isVisible = displayAssetToggle
            recycler.itemAnimator = DefaultItemAnimator()
            if (selectionType != SelectionType.OFFER) {
                recycler.setDivider(R.drawable.recycler_view_divider)
            }
            recycler.addItemDecoration(MarginItemDecoration(12, root.context))
            recycler.layoutManager = LinearLayoutManager(view.context)
            recycler.adapter = fastAdapter.apply {
                addEventHook(object : ClickEventHook<OfferListItem>() {
                    override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                        return if (viewHolder is OfferListItem.ViewHolder) viewHolder.binding.buttonContinue else null
                    }

                    override fun onClick(
                        v: View,
                        position: Int,
                        fastAdapter: FastAdapter<OfferListItem>,
                        item: OfferListItem
                    ) {
                        eventConsumer.accept(ExchangeEvent.OnOfferClicked(item.model, true))
                    }
                })
                onClickListener = { _, _, item, _ ->
                    when (item) {
                        is CountryListItem -> ExchangeEvent.OnCountryClicked(item.model)
                        is RegionListItem -> ExchangeEvent.OnRegionClicked(item.model)
                        is CurrencyListItem -> ExchangeEvent.OnCurrencyClicked(item.model)
                        is OfferListItem -> ExchangeEvent.OnOfferClicked(item.model, false)
                        is AssetListItem -> ExchangeEvent.OnCurrencyClicked(item.model)
                        else -> error("Unsupported item")
                    }.run(eventConsumer::accept)
                    true
                }
            }

            inputSearch.addTextChangedListener { editable ->
                val newText = editable?.toString()
                countriesAdapter.filter(newText)
                assetAdapter.filter(newText)
                currencyAdapter.filter(newText)
                regionAdapter.filter(newText)
            }

            inputSearch.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_SEARCH -> {
                        Utils.hideKeyboard(view.context)
                        true
                    }
                    else -> false
                }
            }

            fastAdapter.getSelectExtension().apply {
                isSelectable = true
                multiSelect = false
            }
        }
    }

    private fun RecyclerView.setDivider(@DrawableRes drawableRes: Int) {
        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        drawable?.let {
            divider.setDrawable(it)
            addItemDecoration(divider)
        }
    }

    override fun ExchangeModel.render(): Unit = with(binding) {
        when (val state = state) {
            is ExchangeModel.State.ConfigureSettings -> {
                when (state.target) {
                    ExchangeModel.ConfigTarget.MENU -> Unit
                    ExchangeModel.ConfigTarget.CURRENCY -> {
                        title.setText(R.string.Exchange_Settings_currency)
                        currencyAdapter.set(state.fiatCurrencies)
                        fastAdapter.getSelectExtension().run {
                            deselect()
                            selectedFiatCurrency?.let { selectedFiatCurrency ->
                                select(state.fiatCurrencies.indexOf(selectedFiatCurrency))
                            }
                        }
                    }
                    ExchangeModel.ConfigTarget.COUNTRY -> {
                        title.setText(R.string.Exchange_Settings_country)
                        countriesAdapter.set(countries)
                        fastAdapter.getSelectExtension().run {
                            deselect()
                            selectedCountry?.let { selectedCountry ->
                                select(countries.indexOf(selectedCountry))
                            }
                        }
                    }
                    ExchangeModel.ConfigTarget.REGION -> {
                        title.setText(R.string.Exchange_Settings_region)
                        val regions = checkNotNull(selectedCountry).regions
                        regionAdapter.set(regions)
                        fastAdapter.getSelectExtension().run {
                            deselect()
                            selectedRegion?.let { selectedRegion ->
                                select(regions.indexOf(selectedRegion))
                            }
                        }
                    }
                }
            }
            is ExchangeModel.State.OrderSetup -> {
                title.setText(R.string.Exchange_offer_offers)
                offersAdapter.set(offerDetails)
                fastAdapter.getSelectExtension().run {
                    deselect()
                    selectedOffer?.let { selectedOffer ->
                        select(offerDetails.indexOf(selectedOffer))
                    }
                }
            }
            is ExchangeModel.State.SelectAsset -> {
                title.setText(R.string.Exchange_selectAsset)
                assetAdapter.set(state.assets)
                fastAdapter.getSelectExtension().run {
                    deselect()
                    (if (state.source) sourceCurrencyCode else quoteCurrencyCode)?.let { currencyCode ->
                        select(state.assets.indexOfFirst { it.code.equals(currencyCode, true) })
                    }
                }

                val activeColor = getColor(R.color.white)
                val inactiveColor = getColor(R.color.hydra_quaternary_background)
                if (state.source) {
                    buttonFrom.backgroundTintList = ColorStateList.valueOf(activeColor)
                    buttonTo.backgroundTintList = ColorStateList.valueOf(inactiveColor)
                    buttonFrom.setTextColor(inactiveColor)
                    buttonTo.setTextColor(activeColor)
                } else {
                    buttonFrom.backgroundTintList = ColorStateList.valueOf(inactiveColor)
                    buttonTo.backgroundTintList = ColorStateList.valueOf(activeColor)
                    buttonFrom.setTextColor(activeColor)
                    buttonTo.setTextColor(inactiveColor)
                }

                sourceCurrencyCode?.let { sourceCurrencyCode ->
                    val currencyIcon = TokenUtil.getTokenIconPath(sourceCurrencyCode, true)
                    if (!currencyIcon.isNullOrBlank()) {
                        viewAttachScope.launch {
                            buttonFrom.iconTint = null
                            buttonFrom.icon = Default { Drawable.createFromPath(currencyIcon) }
                        }
                    }
                }
                quoteCurrencyCode?.let { quoteCurrencyCode ->
                    val currencyIcon = TokenUtil.getTokenIconPath(quoteCurrencyCode, true)
                    if (!currencyIcon.isNullOrBlank()) {
                        viewAttachScope.launch {
                            buttonTo.iconTint = null
                            buttonTo.icon = Default { Drawable.createFromPath(currencyIcon) }
                        }
                    }
                }
            }
            else -> Unit
        }
    }
}
