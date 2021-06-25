/**
 * BreadWallet
 *
 * Created by Alan Hill <alan.hill@breadwallet.com> on 6/7/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.wallet.spark

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.graphics.RectF
import com.breadwallet.model.PriceDataPoint

/**
 * A simple adapter class to display your points in the graph and has support for
 * registering/notifying {@link DataSetObserver}s when data is changed.
 *
 * Adapted from Robinhood's SparkView: https://github.com/robinhood/spark
 */
class SparkAdapter {

    private val dataSetObservable = DataSetObservable()

    var dataSet: List<PriceDataPoint> = emptyList()
    val count: Int
        get() {
            return dataSet.size
        }

    /**
     * Retrieve the Y axis value for the given index
     */
    fun getY(index: Int): Float = dataSet[index].closePrice.toFloat()

    /**
     * Get the boundaries of the entire dataset to be displayed to the user. It is the min and max
     * of the actual data points in the adapter.
     */
    fun getDataBounds(): RectF {
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE

        for (i in 0 until count) {
            val x = i.toFloat()
            minX = Math.min(minX, x)
            maxX = Math.max(maxX, x)

            val y = getY(i)
            minY = Math.min(minY, y)
            maxY = Math.max(maxY, y)
        }

        return RectF(minX, minY, maxX, maxY)
    }

    /**
     * Notify any registered observers that the data has changed.
     */
    fun notifyDataSetChanged() {
        dataSetObservable.notifyChanged()
    }

    /**
     * Notify any registered observers the data is no longer available or invalid
     */
    fun notifyDataSetInvalidated() {
        dataSetObservable.notifyInvalidated()
    }

    /**
     * Register a [DataSetObserver]
     */
    fun registerDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.registerObserver(observer)
    }

    /**
     * Remove the given [DataSetObserver]
     */
    fun unregisterDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.unregisterObserver(observer)
    }
}
