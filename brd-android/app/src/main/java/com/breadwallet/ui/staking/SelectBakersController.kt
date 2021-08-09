/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/24/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.staking

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluelinelabs.conductor.RouterTransaction
import com.brd.bakerapi.models.Baker
import com.breadwallet.databinding.ControllerSelectBakerBinding
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.web.WebController
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.GenericModelAdapter
import com.mikepenz.fastadapter.adapters.ModelAdapter
import com.platform.HTTPServer

interface SelectBakerListener {
    fun onSelected(baker: Baker)
    fun onSelectCancelled()
}

class SelectBakersController(
    val bakers: List<Baker> = emptyList(),
    bundle: Bundle? = null
) : BaseController(bundle) {

    private val binding by viewBinding(ControllerSelectBakerBinding::inflate)

    private var fastAdapter: GenericFastAdapter? = null
    private var bakerAdapter: GenericModelAdapter<Baker>? = null

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        bakerAdapter = ModelAdapter { BakerItem(it) }
        fastAdapter = FastAdapter.with(listOf(bakerAdapter!!))
        with(binding) {
            bakersList.adapter = fastAdapter
            bakersList.itemAnimator = DefaultItemAnimator()
            bakersList.layoutManager = LinearLayoutManager(view.context)
            buttonClose.setOnClickListener { onCancel() }
            buttonFaq.setOnClickListener {
                val supportBaseUrl = HTTPServer.getPlatformUrl(HTTPServer.URL_SUPPORT)
                val url = "$supportBaseUrl/article?slug=staking"
                router.pushController(RouterTransaction.with(WebController(url)))
            }
        }
        bakerAdapter!!.setNewList(bakers)

        fastAdapter!!.onClickListener = { _, _, item, _ ->
            if (item is BakerItem) findListener<SelectBakerListener>()?.onSelected(item.baker)
            router.popCurrentController()
            true
        }
    }

    private fun onCancel() {
        findListener<SelectBakerListener>()?.onSelectCancelled()
        router.popCurrentController()
    }

    override fun handleBack(): Boolean {
        findListener<SelectBakerListener>()?.onSelectCancelled()
        return super.handleBack()
    }

    override fun onDestroyView(view: View) {
        bakerAdapter = null
        fastAdapter = null
        super.onDestroyView(view)
    }
}
