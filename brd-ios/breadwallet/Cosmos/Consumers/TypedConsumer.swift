//
//  TypedConsumer.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation
import Cosmos

class TypedConsumer<T>: Consumer {

    private weak var consumer: Consumer?

    init(_ consumer: Consumer) {
        self.consumer = consumer
    }

    convenience init?(optional consumer: Consumer?) {
        guard let consumer = consumer else {
            return nil
        }
        self.init(consumer)
    }

    func accept(_ typedValue: T) {
        consumer?.accept(value: typedValue)
    }

    func accept(value: Any?) {
        consumer?.accept(value)
    }
}
