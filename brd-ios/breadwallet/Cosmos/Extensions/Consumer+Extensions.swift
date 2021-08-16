//
//  Consumer+Extensions.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation
import Cosmos

extension Consumer {
    
    func accept(_ value: Any?) {
        self.accept(value: value)
    }
}
