// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCPersonalInfoRoutingLogic {
    var dataStore: KYCPersonalInfoDataStore? { get }
    
    func showKYCUploadScene()
}

class KYCPersonalInfoRouter: NSObject, KYCPersonalInfoRoutingLogic {
    weak var viewController: KYCPersonalInfoViewController?
    var dataStore: KYCPersonalInfoDataStore?
    
    func showKYCUploadScene() {
        let kycUploadViewController = KYCUploadViewController()
        viewController?.navigationController?.pushViewController(kycUploadViewController, animated: true)
    }
}
