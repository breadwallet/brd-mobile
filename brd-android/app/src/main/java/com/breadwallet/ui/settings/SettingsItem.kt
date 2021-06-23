/**
 * BreadWallet
 *
 * Created by Pablo Budelli <pablo.budelli@breadwallet.com> on 10/17/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.ui.settings

data class SettingsItem(
    val title: String,
    val option: SettingsOption,
    val iconResId: Int? = null,
    val addOn: String = "",
    val subHeader: String = ""
)
