//
//  UITextField+Extensions.swift
//  breadwallet
//
//  Created by blockexplorer on 15/04/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//	See the LICENSE file at the project root for license information.
//
	
import UIKit

extension UITextField {
    
    func defaultPlaceholder(_ string: String) -> NSAttributedString {
        NSAttributedString(
            string: string,
            attributes: [
                .font: font ?? Theme.body1,
                .foregroundColor: (textColor ?? Theme.primaryText)
                    .withAlphaComponent(0.3)
            ]
        )
    }
    
}
