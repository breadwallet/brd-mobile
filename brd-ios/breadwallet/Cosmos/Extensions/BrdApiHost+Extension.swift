//
//  BrdApiHost+Extensions.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Cosmos

extension BrdApiHost.Custom {

    convenience init?(_ host: String?) {
        guard let host = host else {
            return nil
        }
        self.init(host: host)
    }
}
