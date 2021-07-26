/**
 * BreadWallet
 *
 * Created by Michael Inger <michael.inger@brd.com> on 6/2/21.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.brd.support

import com.brd.support.SupportEffectHandler
import platform.Foundation.*


class IosSupportDataProvider() :  SupportDataProvider {

    fun load(fileName: String): String {
        val (name, extension) = fileName.split(".")
        return NSBundle.main.path(forResource: name, ofType: extension)?.let {
            String(contentsOfFile: it)
        } ?: "[]"
    }
}