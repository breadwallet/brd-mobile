/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 6/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.support

import com.brd.support.SupportModel.*
import kotlinx.serialization.*

data class SupportModel(
    /** Current [State] */
    val state: State = State.Initializing,
    /** List of all available section */
    val sections: List<Section> = emptyList(),
    /** List of articles available via navigation and search */
    val articles: List<Article> = emptyList(),
    /** List of promoted faq articles */
    val faqArticles: List<Article> = emptyList(),
    /** List of secondary articles only accessible via deep link, not nav / search  */
    val secondaryArticles: List<Article> = emptyList(),
    /** User selected section */
    val selectedSection: Section? = null,
    /** User selected article */
    val selectedArticle: Article? = null,
    /** Slug used for deep linking to article */
    val slug: String? = null,
    /** Currency code used for deep linking to article */
    val currencyCode: String? = null,
    /** True if user is viewing a single article without the index. */
    val deepLinked: Boolean = false,
) {

    companion object {
        /**
         * [SupportModel] factory for Swift callers.
         * `Destination` optionally deep link into article or section
         */
        fun create(slug: String? = null, currencyCode: String? = null): SupportModel {
            return SupportModel(
                slug = slug,
                currencyCode = currencyCode,
                deepLinked = !slug.isNullOrBlank() || !currencyCode.isNullOrBlank(),
            )
        }
    }

    sealed class State {
        /** Loading support pages from disk */
        object Initializing : State()

        /** Top level index */
        object Index : State()

        /** Display selected section */
        data class Section(
            val section: SupportModel.Section,
            val articles: List<SupportModel.Article>,
        ) : State()

        /** Display selected article */
        data class Article(val article: SupportModel.Article) : State()

        /** Display search results */
        data class Search(val results: List<SupportModel.Article>) : State() {
            override fun toString(): String {
                return "Search(results=(size:${results.size}))"
            }
        }
    }

    /**
     * Create an event tag appending .[action] to the current mode.
     */
    fun event(action: String): String =
        "Support.$action"

    override fun toString(): String {
        return "SupportModel(" +
            "state=$state, " +
            "slug=$slug, " +
            "currencyCode=$currencyCode, " +
            "selectedSession=$selectedSection, " +
            "selectedArticle=$selectedArticle, " +
            "sections=${sections.size}, " +
            "articles=${articles.size}" +
            ")"
    }

    @Serializable
    data class Section(
        val id: Long,
        @SerialName("name")
        val title: String,
        val position: Int,
    )

    @Serializable
    data class Article(
        val id: Long,
        val title: String,
        @SerialName("section_id")
        val sectionId: Long,
        val promoted: Boolean,
        val position: Int,
        @SerialName("label_names")
        val labelNames: List<String> = emptyList(),
        val body: String,
    ) {
        override fun toString(): String {
            return "Article(" +
                "id=$id, " +
                "title=(size:${title.length}), " +
                "sectionId=$sectionId, " +
                "promoted=$promoted, " +
                "position=$position, " +
                "labelNames=$labelNames, " +
                "body=(size:${body.length})" +
                ")"
        }
    }
}
