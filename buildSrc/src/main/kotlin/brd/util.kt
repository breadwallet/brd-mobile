/**
 * BreadWallet
 *
 * Created by Drew Carlson <drew.carlson@breadwallet.com> on 10/30/20.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package brd

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

fun String.eval(): String =
    Runtime.getRuntime()
        .exec(this)
        .inputStream
        .use { it.readBytes().toString(Charsets.UTF_8) }
        .trim()

fun Project.getExtra(key: String, default: String? = null): String =
    checkNotNull(if (extra.has(key)) extra.get(key) as? String else default) {
        "Expected property '$key' but not found, add -P$key=<value>."
    }
