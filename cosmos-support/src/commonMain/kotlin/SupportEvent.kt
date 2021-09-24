/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 6/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.support

import com.brd.support.SupportModel as M


sealed class SupportEvent {

    /** Feedback for [SupportEffect.LoadArticles]. */
    data class OnArticlesLoaded(
        val sections: List<M.Section>,
        val articles: List<M.Article>,
        val secondaryArticles: List<M.Article>,
    ) : SupportEvent() {
        override fun toString(): String {
            return "OnArticlesLoaded(sections=${sections.count()}, articles=${articles.count()})"
        }
    }

    /** User back navigation event. */
    object OnBackClicked : SupportEvent()

    /** User close support navigation event. */
    object OnCloseClicked : SupportEvent()

    /** User section selection. */
    data class OnSectionClicked(val section: M.Section) : SupportEvent()

    /** User article selection. */
    data class OnArticleClicked(val article: M.Article) : SupportEvent()

    /** User article search event. */
    data class OnSearch(val term: String) : SupportEvent()

    /** Feedback for [SupportEffect.Search]. */
    data class OnSearchResults(val articles: List<M.Article>) : SupportEvent()
}
