//
//  ShankeNoAnimation.swift
//  breadwallet
// 
//  Created by blockexplorer on 04/05/2021.
//  Copyright (c) 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

class ShakeNoAnimation: CAKeyframeAnimation {

    static func animation(distance: CGFloat = 10) -> CAAnimation {
        let animation = CAKeyframeAnimation()
        animation.keyPath = "position.x"
        animation.values = [0, 1, -1, 1, -0.5, 0.5, -0.5, 0].map { $0 * distance }
        animation.keyTimes = [0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1]
        animation.duration = 0.4
        animation.isAdditive = true
        return animation
    }
}
