/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 6/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.support

import platform.Foundation.*


class IosSupportDataProvider :  SupportDataProvider {

    override fun load(fileName: String): String {
        val (name, extension) = fileName.split(".")
        return NSBundle.mainBundle.pathForResource(name, extension)
            ?.let { NSString.stringWithContentsOfFile(it) }
            ?.toString()
            ?: "[]"
    }
}
