/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 8/4/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.support

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.brd.support.AndroidSupportDataProvider
import com.brd.support.SupportEffectHandler
import com.brd.support.SupportInit
import com.brd.support.SupportUpdate
import com.breadwallet.databinding.ControllerSupportBinding
import com.breadwallet.tools.util.EventUtils
import com.breadwallet.tools.util.Utils
import com.breadwallet.ui.MobiusKtController
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ModelAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kt.mobius.Connectable
import kt.mobius.Connection
import kt.mobius.disposables.Disposable
import kt.mobius.extras.CompositeEffectHandler
import kt.mobius.functions.Consumer
import com.brd.support.SupportEffect as F
import com.brd.support.SupportEvent as E
import com.brd.support.SupportModel as M

private const val SLUG = "SupportController.SLUG"
private const val CURRENCY_CODE = "SupportController.CURRENCY_CODE"

class SupportController(args: Bundle? = null) : MobiusKtController<M, E, F>(args) {

    constructor(slug: String, currencyCode: String? = null) :
        this(bundleOf(SLUG to slug, CURRENCY_CODE to currencyCode))

    private val binding by viewBinding(ControllerSupportBinding::inflate)
    private val articleAdapter by resetOnViewDestroy { ModelAdapter(::SupportArticleItem) }
    private val mainTitleAdapter by resetOnViewDestroy { ModelAdapter(::SupportTitleItem) }
    private val groupTitleAdapter by resetOnViewDestroy { ModelAdapter(::SupportTitleItem) }
    private val sectionAdapter by resetOnViewDestroy { ModelAdapter(::SupportSectionItem) }
    private val bodyAdapter by resetOnViewDestroy { ModelAdapter(::SupportArticleBodyItem) }
    private val fastAdapter by resetOnViewDestroy {
        FastAdapter.with(listOf(mainTitleAdapter, articleAdapter, groupTitleAdapter, sectionAdapter, bodyAdapter))
    }

    override val defaultModel = M.create(
        slug = argOptional(SLUG),
        currencyCode = argOptional(CURRENCY_CODE)
    )

    override val init = SupportInit
    override val update = SupportUpdate
    override val effectHandler: Connectable<F, E>
        get() = CompositeEffectHandler.from(
            object : Connectable<F, E> {
                override fun connect(output: Consumer<E>): Connection<F> {
                    return SupportEffectHandler(
                        output,
                        AndroidSupportDataProvider(checkNotNull(applicationContext)),
                        Dispatchers.Default
                    )
                }
            },
            object : Connectable<F, E> {
                override fun connect(output: Consumer<E>): Connection<F> {
                    return object : Connection<F> {
                        override fun accept(value: F) {
                            when (value) {
                                F.ExitFlow -> controllerScope.launch(Main) {
                                    router.popCurrentController()
                                }
                                is F.TrackEvent -> EventUtils.pushEvent(value.name, value.props)
                                else -> Unit // ignored
                            }
                        }

                        override fun dispose() = Unit
                    }
                }
            }
        )

    override fun onCreateView(view: View) {
        super.onCreateView(view)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = fastAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    override fun bindView(output: Consumer<E>): Disposable {
        binding.buttonClose.setOnClickListener {
            output.accept(E.OnCloseClicked)
        }
        binding.inputSearch.doOnTextChanged { text, _, _, _ ->
            output.accept(E.OnSearch(text?.toString().orEmpty()))
        }
        binding.inputSearch.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    Utils.hideKeyboard(v.context)
                    true
                }
                else -> false
            }
        }
        fastAdapter.onClickListener = { view, _, item, pos ->
            when (item) {
                is SupportArticleItem -> {
                    output.accept(E.OnArticleClicked(item.model))
                    true
                }
                is SupportSectionItem -> {
                    output.accept(E.OnSectionClicked(item.model))
                    true
                }
                else -> false
            }
        }
        return Disposable { }
    }

    override fun M.render() = with(binding) {
        ifChanged(M::state) { state ->
            when (state) {
                M.State.Initializing -> {
                    title.text = "Support"
                    loadingView.isVisible = true
                    mainTitleAdapter.clear()
                    groupTitleAdapter.clear()
                    articleAdapter.clear()
                    sectionAdapter.clear()
                    bodyAdapter.clear()
                    recyclerView.scrollToPosition(0)
                }
                M.State.Index -> {
                    title.text = "Support"
                    loadingView.isVisible = false
                    mainTitleAdapter.set(listOf("Frequently Asked Questions"))
                    groupTitleAdapter.set(listOf("Browse Topics"))
                    articleAdapter.set(faqArticles)
                    sectionAdapter.set(sections)
                    bodyAdapter.clear()
                    recyclerView.scrollToPosition(0)
                }
                is M.State.Article -> {
                    loadingView.isVisible = false
                    title.text = selectedArticle?.title
                    mainTitleAdapter.set(listOf(state.article.title))
                    groupTitleAdapter.clear()
                    articleAdapter.clear()
                    sectionAdapter.clear()
                    bodyAdapter.set(listOf(state.article))
                    recyclerView.scrollToPosition(0)
                }
                is M.State.Section -> {
                    loadingView.isVisible = false
                    title.text = state.section.title
                    mainTitleAdapter.set(listOf(state.section.title))
                    articleAdapter.set(state.articles)
                    groupTitleAdapter.clear()
                    sectionAdapter.clear()
                    bodyAdapter.clear()
                    recyclerView.scrollToPosition(0)
                }
                is M.State.Search -> {
                    loadingView.isVisible = false
                    title.text = "Support"
                    articleAdapter.set(state.results)
                    mainTitleAdapter.clear()
                    groupTitleAdapter.clear()
                    sectionAdapter.clear()
                    bodyAdapter.clear()
                    recyclerView.scrollToPosition(0)
                }
            }
        }
    }

    override fun handleBack(): Boolean {
        eventConsumer.accept(E.OnBackClicked)
        return true
    }
}
