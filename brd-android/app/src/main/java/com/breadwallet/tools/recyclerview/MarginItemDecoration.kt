package com.breadwallet.tools.recyclerview

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.breadwallet.tools.util.Utils

class MarginItemDecoration(spaceSize: Int, context: Context) : RecyclerView.ItemDecoration() {

    private val convertedSpace = Utils.getPixelsFromDps(context, spaceSize)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            left = convertedSpace
            right = convertedSpace
        }
    }
}
