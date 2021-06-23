/**
 * BreadWallet
 *
 * Created by Mihail Gutan on <mihail@breadwallet.com> 6/8/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object BRDateUtil {
    private val timeFormat
        get() = SimpleDateFormat("h:mm a", Locale.getDefault())

    private val shortDateFormat
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private val fullDateTimeFormat
        get() = SimpleDateFormat("MMMM dd, yyyy, hh:mm a", Locale.getDefault())

    fun getTime(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = timestamp
        return timeFormat.format(calendar.timeInMillis)
    }

    fun getShortDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = timestamp
        return shortDateFormat.format(calendar.timeInMillis)
    }

    fun getFullDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = timestamp
        return fullDateTimeFormat.format(calendar.timeInMillis)
    }
}