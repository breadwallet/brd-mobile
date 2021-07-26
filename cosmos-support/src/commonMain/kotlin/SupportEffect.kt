/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 6/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.support

sealed class SupportEffect {

    object LoadArticles : SupportEffect()

    data class Search(
        val term: String,
        val articles: List<SupportModel.Article>
    ) : SupportEffect()

    data class TrackEvent(
        val name: String,
        val props: Map<String, String> = emptyMap(),
    ) : SupportEffect()

    object ExitFlow : SupportEffect()
}
