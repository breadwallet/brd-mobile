/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 12/3/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.flowbind

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn

fun TextView.editorActions(): Flow<Int> =
    callbackFlow {
        setOnEditorActionListener { _, actionId, _ ->
            offer(actionId)
        }
        awaitClose { setOnEditorActionListener(null) }
    }.flowOn(Dispatchers.Main)

fun TextView.textChanges(debounceMs: Long = 100L): Flow<String> =
    callbackFlow<String> {
        val listener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                offer(text?.toString() ?: "")
            }
        }
        addTextChangedListener(listener)
        awaitClose { removeTextChangedListener(listener) }
    }.flowOn(Dispatchers.Main)
        .debounce(debounceMs)

fun TextView.focusChanges(): Flow<Boolean> =
    callbackFlow<Boolean> {
        setOnFocusChangeListener { _, hasFocus ->
            offer(hasFocus)
        }

        awaitClose { onFocusChangeListener = null }
    }
