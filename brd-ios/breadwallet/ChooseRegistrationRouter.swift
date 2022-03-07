//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol ChooseRegistrationRoutingLogic {
    var dataStore: ChooseRegistrationDataStore? { get }
}

class ChooseRegistrationRouter: NSObject, ChooseRegistrationRoutingLogic {
    weak var viewController: ChooseRegistrationViewController?
    var dataStore: ChooseRegistrationDataStore?
}
