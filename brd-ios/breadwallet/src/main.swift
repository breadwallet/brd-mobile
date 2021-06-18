//
//  Main.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-02-17.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

private func delegateClassName() -> String? {
    return NSClassFromString("XCTestCase") == nil ? NSStringFromClass(AppDelegate.self) : nil
}

private let unsafeArgv = UnsafeMutableRawPointer(CommandLine.unsafeArgv)
                            .bindMemory(
                                to: UnsafeMutablePointer<Int8>.self,
                                capacity: Int(CommandLine.argc))

_ = UIApplicationMain(CommandLine.argc, CommandLine.unsafeArgv, nil, delegateClassName())
