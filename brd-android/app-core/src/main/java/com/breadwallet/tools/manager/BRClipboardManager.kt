/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/14/15.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.manager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.lang.Exception
import kotlin.properties.Delegates

object BRClipboardManager {
    private var context by Delegates.notNull<Context>()
    private val clipboard by lazy {
        context.getSystemService(ClipboardManager::class.java)
    }

    fun provideContext(context: Context) {
        this.context = context
    }

    fun putClipboard(text: String?) {
        try {
            val clip = ClipData.newPlainText("message", text)
            clipboard.setPrimaryClip(clip)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getClipboard(): String {
        // Gets a content resolver instance
        val cr = context.contentResolver

        // Gets the clipboard data from the clipboard
        val clip = clipboard.primaryClip
        if (clip != null) {

            // Gets the first item from the clipboard data
            val item = clip.getItemAt(0)
            return coerceToText(item).toString()
        }
        return ""
    }

    private fun coerceToText(item: ClipData.Item): CharSequence {
        // If this Item has an explicit textual value, simply return that.
        val text = item.text
        return text ?: "no text"
    }
}