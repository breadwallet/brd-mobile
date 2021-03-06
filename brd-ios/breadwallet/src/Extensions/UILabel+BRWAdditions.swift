//
//  UILabel+BRWAdditions.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2016-10-26.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

extension UILabel {
    static func wrapping(font: UIFont, color: UIColor) -> UILabel {
        let label = UILabel()
        label.numberOfLines = 0
        label.lineBreakMode = .byWordWrapping
        label.font = font
        label.textColor = color
        return label
    }

    static func wrapping(font: UIFont) -> UILabel {
        let label = UILabel()
        label.numberOfLines = 0
        label.lineBreakMode = .byWordWrapping
        label.font = font
        return label
    }

    convenience init(font: UIFont) {
        self.init()
        self.font = font
    }

    convenience init(
            text: String? = nil,
            font: UIFont = Theme.body1,
            color: UIColor = Theme.primaryText
    ) {
        self.init()
        self.text = text
        self.font = font
        self.textColor = color
    }

    func pushNewText(_ newText: String) {
        let animation: CATransition = CATransition()
        animation.timingFunction = CAMediaTimingFunction(name:
            CAMediaTimingFunctionName.easeInEaseOut)
        animation.type = CATransitionType.push
        animation.subtype = CATransitionSubtype.fromTop
        animation.duration = C.animationDuration
        layer.add(animation, forKey: CATransitionType.push.rawValue)
        text = newText
    }
}
