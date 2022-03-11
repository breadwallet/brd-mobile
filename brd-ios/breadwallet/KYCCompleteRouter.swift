// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCCompleteRoutingLogic {
    var dataStore: KYCCompleteDataStore? { get }
    
    func dismissFlow()
}

class KYCCompleteRouter: NSObject, KYCCompleteRoutingLogic {
    weak var viewController: KYCCompleteViewController?
    var dataStore: KYCCompleteDataStore?
    
    func dismissFlow() {
        viewController?.navigationController?.dismiss(animated: true)
    }
}
