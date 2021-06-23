//
//  UnEditableTextView.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2017-04-04.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class UnEditableTextView: UITextView {
    override var canBecomeFirstResponder: Bool {
        return false
    }
}
