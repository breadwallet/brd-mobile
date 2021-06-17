//
//  UIScreen+Additions.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-09-28.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

extension UIScreen {
    var safeWidth: CGFloat {
        return min(bounds.width, bounds.height)
    }
}
