//
//  NSMutableAttributedString+Additions.swift
//  breadwallet
//
//  Created by Ehsan Rezaie on 2018-01-23.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Foundation

extension NSMutableAttributedString {
    func set(attributes attrs: [NSAttributedString.Key: Any], forText text: String) {
        if let range = self.string.range(of: text) {
            setAttributes(attrs, range: NSRange(range, in: self.string))
        }
    }
}
