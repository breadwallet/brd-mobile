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

    static func close(_ target: Any? = nil, action: Foundation.Selector? = nil) -> UIBarButtonItem {
        let accessibilityLabel = E.isScreenshots ? "Close" : S.AccessibilityLabels.close
        let button = UIBarButtonItem(
            image: #imageLiteral(resourceName: "CloseModern"),
            style: .plain,
            target: target,
            action: action
        )
        button.accessibilityLabel = accessibilityLabel
        return button
    }

    convenience init(
        _ barButtonSystemItem: UIBarButtonItem.SystemItem,
        target: Any? = nil,
        action: Foundation.Selector? = nil
    ) {
        self.init(barButtonSystemItem: barButtonSystemItem, target: target, action: action)
    }

    convenience init(_ system: UIBarButtonItem.SystemItem, onTap: (() -> Void)? = nil) {
        self.init(barButtonSystemItem: system, target: nil, action: nil)
        tap = onTap
    }

    convenience init(_ title: String, onTap: (() -> Void)? = nil) {
        self.init(title: title, style: .plain, target: nil, action: nil)
        tap = onTap
    }

    convenience init(_ image: UIImage?, onTap: (() -> Void)? = nil) {
        self.init(image: image, style: .plain, target: nil, action: nil)
        tap = onTap
    }

    convenience init (closeWithAction onTap: (() -> Void)? = nil) {
        self.init(UIImage(named: "CloseModern"), onTap: onTap)
        accessibilityLabel = E.isScreenshots ? "Close" : S.AccessibilityLabels.close
    }
}
