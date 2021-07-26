// 
//  NotificationCenter+Extensions.swift
//  breadwallet
//
//  Created by stringcode on 06/04/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import Foundation
import UIKit

extension NotificationCenter {

    func add(
        observer: Any,
        selector aSelector: Foundation.Selector,
        name aName: NSNotification.Name?,
        object anObject: Any? = nil
    ) {
        addObserver(observer, selector: aSelector, name: aName, object: anObject)
    }
}
