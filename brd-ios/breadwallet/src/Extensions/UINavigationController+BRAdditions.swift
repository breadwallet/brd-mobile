//
//  UINavigationController+BRAdditions.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2016-11-29.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

extension UINavigationController {

    func setDefaultStyle() {
        setClearNavbar()
        navigationBar.tintColor = .darkText
        navigationBar.titleTextAttributes = [
            NSAttributedString.Key.font: UIFont.header
        ]
    }

    func setWhiteStyle() {
        navigationBar.tintColor = .white
        navigationBar.titleTextAttributes = [
            NSAttributedString.Key.foregroundColor: UIColor.white,
            NSAttributedString.Key.font: UIFont.header
        ]
    }
    
    func setDarkStyle(
        _ backgroundColor: UIColor? = .navigationBackground,
        titleFont: UIFont = UIFont.header,
        hideShadow: Bool = false
    ) {
        let titleTextAttributes = [
            NSAttributedString.Key.foregroundColor: Theme.primaryText,
            NSAttributedString.Key.font: titleFont
        ]

        navigationBar.isTranslucent = false
        navigationBar.barStyle = .blackOpaque
        navigationBar.barTintColor = backgroundColor
        navigationBar.tintColor = .navigationTint
        navigationBar.titleTextAttributes = titleTextAttributes

        if hideShadow {
            navigationBar.shadowImage = UIImage(named: "TransparentPixel")
        }

        view.backgroundColor = backgroundColor

        if #available(iOS 13.0, *) {
            guard hideShadow else {
                return
            }
            let appearance = UINavigationBarAppearance()
            appearance.backgroundColor = backgroundColor
            appearance.titleTextAttributes = titleTextAttributes
            appearance.largeTitleTextAttributes = titleTextAttributes
            appearance.shadowImage = UIImage(named: "TransparentPixel")
            navigationController?.navigationBar.scrollEdgeAppearance = appearance
            navigationController?.navigationBar.compactAppearance = appearance
            navigationController?.navigationBar.standardAppearance = appearance
        }
    }

    func setClearNavbar() {
        navigationBar.setBackgroundImage(UIImage(), for: .default)
        navigationBar.shadowImage = UIImage()
        navigationBar.isTranslucent = true
    }

    convenience init(
        darkWith rootViewController: UIViewController,
        bgColor: UIColor = Theme.quaternaryBackground,
        titleFont: UIFont = .customMedium(size: 18),
        hideShadow: Bool = true
    ) {
        self.init(rootViewController: rootViewController)
        self.setDarkStyle(bgColor, titleFont: titleFont, hideShadow: hideShadow)
    }
}
