// 
//  UIImageView+Extensions.swift
//  breadwallet
//
//  Created by stringcode on 25/03/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

extension UIImageView {

    convenience init(systemName: String) {
        if #available(iOS 13.0, *) {
            self.init(image: UIImage(systemName: systemName))
        } else {
            self.init()
        }
    }
    
    convenience init(name: String) {
        self.init(image: UIImage(named: name))
    }
}
