//
//  CGContext+Additions.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2016-12-13.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

extension CGContext {
    func addLineThrough(_ points: [(CGFloat, CGFloat)]) {
        guard let first = points.first else { return }
        move(to: CGPoint(x: first.0, y: first.1))
        points.dropFirst().forEach {
            addLine(to: CGPoint(x: $0.0, y: $0.1))
        }
    }
}
