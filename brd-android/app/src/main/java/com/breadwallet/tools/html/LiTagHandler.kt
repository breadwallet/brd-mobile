/*
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 09/15/2021
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.html

import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.style.BulletSpan
import org.xml.sax.XMLReader

/**
 * [Html.TagHandler] implementation that processes <ul> and <li> tags and creates bullets.
 *
 * Note: This class is only applied on SDK < 25
 * and processes only one-level list, nested lists do not work correctly.
 */
class LiTagHandler : Html.TagHandler {
    /**
     * Helper marker class. Idea inspired from [Html.fromHtml] implementation
     */
    class Bullet

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
        if (tag == "li" && opening) {
            output.setSpan(
                Bullet(),
                output.length,
                output.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        if (tag == "li" && !opening) {
            output.append("\n\n")
            val lastMark = output.getSpans(0, output.length, Bullet::class.java).lastOrNull()
            lastMark?.let {
                val start = output.getSpanStart(it)
                output.removeSpan(it)
                if (start != output.length) {
                    output.setSpan(
                        BulletSpan(), start, output.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }
}
