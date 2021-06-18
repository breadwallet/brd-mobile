/**
 * BreadWallet
 *
 * Created by Shivangi Gandhi on <shivangi@brd.com> 7/25/18.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.protocols.messageexchange.entities

import kotlinx.parcelize.Parcelize

@Parcelize
class LinkMetaData(
    val serviceMetaData: ServiceMetaData?
) : MetaData("")