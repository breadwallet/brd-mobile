/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/10/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.util

import java.text.Normalizer
import java.util.regex.Pattern

private val EMAIL_REGEX =
    "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"

/** Return true if the string has a valid email format */
fun String?.isValidEmail(): Boolean {
    return !isNullOrBlank() && Pattern.compile(EMAIL_REGEX).matcher(this).matches()
}

fun List<String>.asNormalizedString() =
    Normalizer.normalize(
        joinToString(" ")
            .replace("　", " ")
            .replace("\n", " ")
            .trim()
            .replace(" +".toRegex(), " "), Normalizer.Form.NFKD
    )

fun String.normalize() =
    Normalizer.normalize(this, Normalizer.Form.NFKD)
