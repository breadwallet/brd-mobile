/**
 * BreadWallet
 *
 * Created by Ahsan Butt <ahsan.butt@breadwallet.com> on 3/24/21.
 * Copyright (c) 2021 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.breadwallet.ui.staking

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluelinelabs.conductor.RouterTransaction
import com.brd.bakerapi.models.Baker
import com.breadwallet.databinding.ControllerSelectBakerBinding
import com.breadwallet.logger.logError
import com.breadwallet.ui.BaseController
import com.breadwallet.ui.uistaking.ConfirmationListener
import com.breadwallet.ui.wallet.TransactionListItem
import com.breadwallet.ui.wallet.spark.SparkAdapter
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
