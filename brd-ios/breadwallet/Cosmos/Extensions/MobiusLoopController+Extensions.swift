//
//  MobiusLoopController+Extensions.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//
	
import Foundation
import Cosmos

extension MobiusLoopController {
    
    func startIfNeeded() {
        guard !isRunning else {
            return
        }
        start()
    }
    
    func stopIfNeeded() {
        guard isRunning else {
            return
        }
        stop()
    }
}

func mobiusLoopController(
    loopFactory: MobiusLoopFactory,
    defaultModel: Any,
    view: Connectable
) -> MobiusLoopController {
    let controller = Mobius().controller(
        loopFactory: loopFactory,
        defaultModel: defaultModel
    )
    controller.connect(view: view)
    return controller
}
