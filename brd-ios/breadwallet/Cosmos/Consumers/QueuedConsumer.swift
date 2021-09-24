//
//  QueuedConsumer.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation
import Cosmos

class QueuedConsumer: Consumer {

    var events: [Any?] = []

    func dequeueAll(consumer: Consumer) {
        let dequeued = self.events
        events = []
        dequeued.forEach { consumer.accept(value: $0) }
    }

    func accept(value: Any?) {
        events.append(value)
    }
}
