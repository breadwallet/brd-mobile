//
//  ConsumerDelegate.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation
import Cosmos

class ConsumerDelegate: Consumer {

    var consumer: Consumer?

    init(_ consumer: Consumer? = nil) {
        self.consumer = consumer
    }

    func accept(value: Any?) {
        consumer?.accept(value: value)
    }
}
