//
//  UIScrollView+Additions.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-12-14.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

extension UIScrollView {
    func verticallyOffsetContent(_ deltaY: CGFloat) {
        contentOffset = CGPoint(x: contentOffset.x, y: contentOffset.y - deltaY)
        contentInset = UIEdgeInsets(top: contentInset.top + deltaY, left: contentInset.left, bottom: contentInset.bottom, right: contentInset.right)
        scrollIndicatorInsets = contentInset
    }
}
