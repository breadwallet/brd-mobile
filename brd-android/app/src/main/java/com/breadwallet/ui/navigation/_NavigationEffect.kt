/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 11/6/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.navigation

import com.breadwallet.tools.animation.UiUtils
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.util.isErc20
import com.platform.HTTPServer
import java.util.Locale

/** Returns the full support URL for the articleId and currencyCode. */
fun NavigationTarget.SupportPage.asSupportUrl() = buildString {
    append(HTTPServer.getPlatformUrl(HTTPServer.URL_SUPPORT))
    if (articleId.isNotBlank()) {
        append(UiUtils.ARTICLE_QUERY_STRING)
        append(articleId)

        val currencyCode = currencyCode ?: ""
        if (currencyCode.isNotBlank()) {
            val codeOrErc20 = if (currencyCode.isErc20()) {
                BRConstants.CURRENCY_ERC20
            } else {
                currencyCode.toLowerCase(Locale.ROOT)
            }

            append("${UiUtils.CURRENCY_QUERY_STRING}$codeOrErc20")
        }
    }
}
