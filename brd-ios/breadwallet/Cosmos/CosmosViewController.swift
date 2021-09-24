//
//  ConsmosViewController.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

class CosmosViewController: UIViewController, EventSource, Disposable {

    var loopController: MobiusLoopController? = nil
    var eventConsumerDelegate: ConsumerDelegate? = ConsumerDelegate(QueuedConsumer())

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        loopController?.startIfNeeded()
    }

    func consumer() -> Consumer? {
        self.eventConsumerDelegate?.consumer
    }

    func disconnect() {
        eventConsumerDelegate?.consumer = nil
        eventConsumerDelegate = nil
        loopController?.stopIfNeeded()
        loopController?.disconnect()
        loopController = nil
    }

    deinit {

    }

    // MARK: - EventSource

    func subscribe(eventConsumer: Consumer) -> Disposable {
        let queuedConsumer = eventConsumerDelegate?.consumer as? QueuedConsumer
        queuedConsumer?.dequeueAll(consumer: eventConsumer)
        eventConsumerDelegate?.consumer = eventConsumer
        return self
    }

    // MARK: - Disposable

    func dispose() {
        eventConsumerDelegate?.consumer = nil
    }
}

class CosmosTableViewController: UITableViewController, EventSource, Disposable {

    var loopController: MobiusLoopController?
    var eventConsumerDelegate: ConsumerDelegate? = ConsumerDelegate(QueuedConsumer())

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        loopController?.startIfNeeded()
    }

    func consumer() -> Consumer? {
        self.eventConsumerDelegate?.consumer
    }

    func disconnect() {
        eventConsumerDelegate?.consumer = nil
        eventConsumerDelegate = nil
        loopController?.stopIfNeeded()
        loopController?.disconnect()
        loopController = nil
    }

    // MARK: - EventSource

    func subscribe(eventConsumer: Consumer) -> Disposable {
        let queuedConsumer = eventConsumerDelegate?.consumer as? QueuedConsumer
        queuedConsumer?.dequeueAll(consumer: eventConsumer)
        eventConsumerDelegate?.consumer = eventConsumer
        return self
    }

    // MARK: - Disposable

    func dispose() {
        eventConsumerDelegate?.consumer = nil
    }
}

