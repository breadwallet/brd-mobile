/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 6/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.support

import com.brd.support.SupportEvent.*
import com.brd.support.SupportModel.Article
import com.brd.support.SupportModel.State
import kt.mobius.Next
import kt.mobius.Next.Companion.dispatch
import kt.mobius.Next.Companion.next
import kt.mobius.Next.Companion.noChange
import kt.mobius.Update
import com.brd.support.SupportEffect as F
import com.brd.support.SupportEvent as E
import com.brd.support.SupportModel as M


object SupportUpdate : Update<M, E, F> {

    override fun update(model: M, event: E): Next<M, F> {
        return when (event) {
            is OnArticlesLoaded -> onArticlesLoaded(model, event)
            is OnBackClicked -> onBackClicked(model, event)
            is OnCloseClicked -> onCloseClicked(model, event)
            is OnSectionClicked -> onSectionClicked(model, event)
            is OnArticleClicked -> onArticleClicked(model, event)
            is OnSearch -> onSearch(model, event)
            is OnSearchResults -> onSearchResult(model, event)
            else -> noChange()
        }
    }
}

private fun onArticlesLoaded(model: M, event: OnArticlesLoaded): Next<M, F> {
    return when (model.state) {
        is State.Initializing -> {
            var state: State = State.Index
            var selectedArticle: Article? = null
            var currArticles = emptyList<Article>()
            val slugArticles = if (model.slug == null) {
                emptyList()
            } else {
                event.secondaryArticles.filter {
                    it.labelNames.contains("slug:${model.slug}")
                }
            }
            if (slugArticles.isNotEmpty() && model.currencyCode != null) {
                val currencyLabel = "curr:${model.currencyCode.lowercase()}"
                currArticles = slugArticles.filter {
                    it.labelNames.contains(currencyLabel)
                }
            }
            if (slugArticles.isNotEmpty()) {
                selectedArticle = currArticles.firstOrNull() ?: slugArticles.first()
                state = State.Article(article = selectedArticle)
            }
            next(
                model.copy(
                    state = state,
                    sections = event.sections,
                    articles = event.articles,
                    faqArticles = event.articles.filter(Article::promoted),
                    secondaryArticles = event.secondaryArticles,
                    selectedArticle = selectedArticle,
                    slug = null,
                    currencyCode = null,
                ),
                setOfNotNull(
                    if (model.slug == null) {
                        null
                    } else {
                        F.TrackEvent("helpButton", mapOf("articleId" to model.slug))
                    }
                )
            )
        }
        else -> noChange()
    }
}

private fun onBackClicked(model: M, event: OnBackClicked): Next<M, F> {
    return when (model.state) {
        is State.Article -> {
            val nextState: M.State = model.selectedSection?.run(State::Section) ?: State.Index
            next(
                model.copy(
                    state = nextState,
                    selectedArticle = null
                )
            )
        }
        is State.Section -> {
            next(model.copy(state = State.Index, selectedSection = null))
        }
        is State.Search -> {
            val nextState = model.selectedArticle?.run(State::Article)
                ?: model.selectedSection?.run(State::Section)
                ?: State.Index
            next(model.copy(state = nextState))
        }
        else -> dispatch(F.ExitFlow)
    }
}

private fun onCloseClicked(model: M, event: E): Next<M, F> {
    return dispatch(F.ExitFlow)
}

private fun onSectionClicked(model: M, event: OnSectionClicked): Next<M, F> {
    return next(
        model.copy(
            state = State.Section(event.section),
            selectedSection = event.section,
            selectedArticle = null,
        )
    )
}

private fun onArticleClicked(model: M, event: OnArticleClicked): Next<M, F> {
    return next(
        model.copy(
            state = State.Article(event.article),
            selectedArticle = event.article,
        )
    )
}

private fun onSearch(model: M, event: OnSearch): Next<M, F> {
    return next(
        model.copy(state = State.Search(emptyList())),
        setOfNotNull(F.Search(event.term.lowercase(), model.articles))
    )
}

private fun onSearchResult(model: M, event: OnSearchResults): Next<M, F> {
    return next(model.copy(state = State.Search(event.articles)))
}
