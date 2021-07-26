// 
//  UIEdgeInsets+Extensions.swift
//  breadwallet
//
//  Created by stringcode on 24/03/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

extension UIEdgeInsets {
    
    init(_ horizontal: CGFloat, _ vertical: CGFloat) {
        self.init(top: vertical, left: horizontal, bottom: vertical, right: horizontal)
    }
    
    init(forConstrains hVal: CGFloat, vVal: CGFloat? = nil) {
        let vVal = vVal ?? hVal
        self.init(top: vVal, left: hVal, bottom: -vVal, right: -hVal)
    }

    init(aTop: CGFloat = 0, left: CGFloat = 0, bottom: CGFloat = 0, right: CGFloat = 0) {
        self.init(top: aTop, left: left, bottom: bottom, right: right)
    }
}
