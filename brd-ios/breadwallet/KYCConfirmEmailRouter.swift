//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCConfirmEmailRoutingLogic {
    var dataStore: KYCConfirmEmailDataStore? { get }
}

class KYCConfirmEmailRouter: NSObject, KYCConfirmEmailRoutingLogic {
    weak var viewController: KYCConfirmEmailViewController?
    var dataStore: KYCConfirmEmailDataStore?
}
