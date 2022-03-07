// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCAddressRoutingLogic {
    var dataStore: KYCAddressDataStore? { get }
    
    func showKYCPersonalInfoScene()
}

class KYCAddressRouter: NSObject, KYCAddressRoutingLogic {
    weak var viewController: KYCAddressViewController?
    var dataStore: KYCAddressDataStore?
    
    func showKYCPersonalInfoScene() {
        let kycPersonalInfoViewController = KYCPersonalInfoViewController()
        
        kycPersonalInfoViewController.didSetDateAndTaxId = { [weak self] date, taxId in
            self?.dataStore?.dateOfBirth = date
            self?.dataStore?.taxIdNumber = taxId
            
            self?.viewController?.submitData()
        }
        
        viewController?.navigationController?.pushViewController(kycPersonalInfoViewController, animated: true)
    }
}
