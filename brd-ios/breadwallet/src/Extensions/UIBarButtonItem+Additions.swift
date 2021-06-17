//
//  UIBarButtonItem+Additions.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-04-24.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

extension UIBarButtonItem {
    static var negativePadding: UIBarButtonItem {
        let padding = UIBarButtonItem(barButtonSystemItem: .fixedSpace, target: nil, action: nil)
        padding.width = -16.0
        return padding
    }
}
