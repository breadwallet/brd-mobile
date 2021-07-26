/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 6/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.support

import com.brd.support.SupportEffect.*
import com.brd.support.SupportEvent.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kt.mobius.Connection
import kt.mobius.functions.Consumer
import kotlin.native.concurrent.SharedImmutable


private const val SEARCH_DEBOUNCE_MS = 250L
private const val SECONDARY_ARTICLES = "secondary_articles.json"
private const val MAIN_ARTICLES = "main_articles.json"
private const val SECTIONS = "sections.json"

@SharedImmutable
private val json = Json {
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

interface SupportDataProvider {
    fun load(fileName: String): String
}

class SupportEffectHandler(
    private val output: Consumer<SupportEvent>,
    private val supportDataProvider: SupportDataProvider,
    dispatcher: CoroutineDispatcher,
) : Connection<SupportEffect> {

    constructor(
        output: Consumer<SupportEvent>,
        supportDataProvider: SupportDataProvider,
    ) : this(output, supportDataProvider, Main)

    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val searchFlow = MutableSharedFlow<SupportEffect.Search>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        searchFlow
            .debounce(SEARCH_DEBOUNCE_MS)
            .mapLatest { effect ->
                if (effect.term.length < 2) {
                    OnSearchResults(emptyList())
                } else {
                    Default {
                        OnSearchResults(
                            effect.articles.filter {
                                fuzzyMatch(effect.term, it.title)
                            } + effect.articles.filter {
                                fuzzyMatch(effect.term, it.body)
                            }
                        )
                    }
                }
            }
            .onEach { output.accept(it) }
            .launchIn(scope)
    }

    override fun accept(value: SupportEffect) {
        when (value) {
            is LoadArticles -> scope.launch { output.accept(loadArticles(supportDataProvider)) }
            is Search -> scope.launch { searchFlow.emit(value) }
            is TrackEvent -> Unit // native
            ExitFlow -> Unit // native
        }
    }

    override fun dispose() {
        scope.cancel()
    }
}

private suspend fun loadArticles(
    supportDataProvider: SupportDataProvider
): SupportEvent = Default {
    OnArticlesLoaded(
        sections = json.decodeFromString(supportDataProvider.load(SECTIONS)),
        articles = json.decodeFromString(supportDataProvider.load(MAIN_ARTICLES)),
        secondaryArticles = json.decodeFromString(supportDataProvider.load(SECONDARY_ARTICLES)),
    )
}

private fun fuzzyMatch(needle: String, inString: String): Boolean {
    if (needle.isEmpty()) {
        return true
    }
    val remainder = needle.toCharArray().toMutableList()
    for (char in inString) {
        if (char.lowercaseChar() == remainder.first().lowercaseChar()) {
            remainder.removeFirstOrNull()
            if (remainder.isEmpty()) {
                return true
            }
        }
    }
    return false
}
