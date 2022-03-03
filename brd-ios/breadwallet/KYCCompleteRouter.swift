// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCCompleteRoutingLogic {
    var dataStore: KYCCompleteDataStore? { get }
}

class KYCCompleteRouter: NSObject, KYCCompleteRoutingLogic {
    weak var viewController: KYCCompleteViewController?
    var dataStore: KYCCompleteDataStore?
}
