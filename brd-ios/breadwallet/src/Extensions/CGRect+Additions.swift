//
//  CGRect+Additions.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2016-11-29.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

extension CGRect {
    var center: CGPoint {
        return CGPoint(x: self.midX, y: self.midY)
    }

    func expandVertically(_ deltaY: CGFloat) -> CGRect {
        var newFrame = self
        newFrame.origin.y -= deltaY
        newFrame.size.height += deltaY
        return newFrame
    }
}

extension CGRect {

    var minXminY: CGPoint {
        return .init(x: minX, y: minY)
    }

    var maxXminY: CGPoint {
        return .init(x: maxX, y: minY)
    }

    var minXmaxY: CGPoint {
        return .init(x: minX, y: maxY)
    }

    var maxXmaxY: CGPoint {
        return .init(x: maxX, y: maxY)
    }

    var midXminY: CGPoint {
        return .init(x: midX, y: minY)
    }

    var midXmaxY: CGPoint {
        return .init(x: midX, y: maxY)
    }
}
