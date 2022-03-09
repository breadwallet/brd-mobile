// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCSignUpRoutingLogic {
    var dataStore: KYCSignUpDataStore? { get }
}

class KYCSignUpRouter: NSObject, KYCSignUpRoutingLogic {
    weak var viewController: KYCSignUpViewController?
    var dataStore: KYCSignUpDataStore?
}
