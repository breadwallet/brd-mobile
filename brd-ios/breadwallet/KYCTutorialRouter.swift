// 
// Created by Equaleyes Solutions Ltd
// 

import UIKit

protocol KYCTutorialRoutingLogic {
    var dataStore: KYCTutorialDataStore? { get }
    
    func showKYCAddressScene()
    func dismissFlow()
}

class KYCTutorialRouter: NSObject, KYCTutorialRoutingLogic {
    weak var viewController: KYCTutorialViewController?
    var dataStore: KYCTutorialDataStore?
    
    func showKYCAddressScene() {
        let kycAddressViewController = KYCAddressViewController()
        viewController?.navigationController?.pushViewController(kycAddressViewController, animated: true)
    }
    
    func dismissFlow() {
        viewController?.navigationController?.dismiss(animated: true)
    }
}
