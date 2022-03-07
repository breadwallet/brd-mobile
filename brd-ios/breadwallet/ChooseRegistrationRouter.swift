//
//  ChooseRegistrationRouter.swift
//  breadwallet
//
//  Created by Dijana Angelovska on 7.3.22.
//  Copyright (c) 2022 Breadwinner AG. All rights reserved.
//

import UIKit

protocol ChooseRegistrationRoutingLogic {
    var dataStore: ChooseRegistrationDataStore? { get }
}

class ChooseRegistrationRouter: NSObject, ChooseRegistrationRoutingLogic {
    weak var viewController: ChooseRegistrationViewController?
    var dataStore: ChooseRegistrationDataStore?
}
