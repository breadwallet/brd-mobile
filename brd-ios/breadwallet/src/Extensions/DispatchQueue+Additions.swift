//
//  DispatchQueue+Additions.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-04-20.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation

extension DispatchQueue {
    static var walletQueue: DispatchQueue = {
        return DispatchQueue(label: C.walletQueue)
    }()
}
