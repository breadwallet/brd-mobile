/*
 * BreadWallet
 *
 * Created by Amit Goel <amit.goel@breadwallet.com> on 09/15/2021
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.recyclerview

import android.graphics.Canvas
import android.graphics.drawable.InsetDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.breadwallet.appcore.R

class DividerItemDecorator(
    private val horizontalInset: Int = 0,
    private val verticalInset: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        val drawable = ContextCompat.getDrawable(parent.context, R.drawable.recycler_view_divider)
        val divider =
            InsetDrawable(drawable, horizontalInset, verticalInset, horizontalInset, verticalInset)

        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0..childCount - 2) {
            val child: View = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop: Int = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + (divider.intrinsicHeight)
            divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            divider.draw(canvas)
        }
    }
}
