/**
 * BreadWallet
 *
 * Created by Mihail Gutan on <mihail@breadwallet.com> 7/18/18.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.protocols.messageexchange.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.HashMap

@Parcelize
class ServiceMetaData(
    var url: String,
    var name: String,
    var hash: String,
    var createdTime: String,
    var updatedTime: String,
    var logoUrl: String,
    var description: String,
    var domains: List<String>,
    var capabilities: List<Capability>
) : MetaData("") {

    @Parcelize
    class Capability(
        var name: String,
        var scopes: HashMap<String, String>,
        var description: String
    ) : Parcelable
}