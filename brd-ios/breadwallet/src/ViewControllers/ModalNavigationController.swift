//
//  ModalNavigationController.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2016-11-23.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ModalNavigationController: UINavigationController {
    override var preferredStatusBarStyle: UIStatusBarStyle {
        guard let vc = topViewController else { return .default }
        return vc.preferredStatusBarStyle
    }
}
