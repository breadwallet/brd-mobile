/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 2/26/21.
 * Copyright (c) 2021 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brd.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import com.breadwallet.R


@Composable
fun ButterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) colors else lightColors,
        typography = typography,
        shapes = shapes,
        content = content,
    )
}

object ButterColors {
    /** Used for primary buttons, Icons, links, etc */
    val UiAccent = Color(0xFF5B6DEE)

    /** Used for success states & success messages. */
    val UiSuccess = Color(0xFF0EBF15)

    /** Used for error states & error messages. */
    val UiError = Color(0xFFEA5454)

    /** Text & icons color on dark backgrounds. Used to highlight the most important information (e.x. navigation icons, titles, values, etc..) */
    val TextPrimary = Color.White

    /** Secondary texts & Icons color on dark backgrounds. */
    val TextSecondary = Color(0xBFFFFFFF)

    /** Texts and icons inside components on dark backgrounds (e.x. Search field, Input field, Notification etc..) */
    val TextTertiary = Color(0x99FFFFFF)

    /** Text & icons color on light backgrounds. Used to highlight the most important information (e.x. navigation icons, titles, values, etc..) */
    val TextPrimaryDark = Color(0xFF141233)

    /** Secondary texts & Icons color on light backgrounds. */
    val TextSecondaryDark = Color(0x99141233)


    /** Texts and icons inside components on light backgrounds (e.x. Search field, input field, etc..) */
    val TextTertiaryDark = Color(0x66141233)

    /** Primary background color. */
    val BgPrimary = Color(0xFF141233)

    /** Used for components and notification bg’s on dark backgrounds */
    val BgSecondary = Color(0xFF211F3F)

    /** Used only for components (e.x. input) on Secondary bg */
    val BgTertiary = Color(0xFF312F4C)

    /** Used as bg’s primary color for tabbar, popups, popovers, notifications or entire screens. */
    val BgPrimaryLight = Color.White

    /** Used for components on light backgrounds */
    val BgSecondaryLight = Color(0xFFEFEFF2)
}

val lightColors = lightColors(
    background = ButterColors.BgPrimaryLight,
    primary = ButterColors.UiAccent,
    secondary = ButterColors.UiAccent,
    error = ButterColors.UiError
)
val colors = darkColors(
    background = ButterColors.BgPrimary,
    primary = ButterColors.UiAccent,
    secondary = ButterColors.UiAccent,
    error = ButterColors.UiError,
)

val typography = Typography(
    defaultFontFamily = Font(R.font.mobile_font).toFontFamily(),
)

val shapes = Shapes(
    large = RoundedCornerShape(4.dp)
)