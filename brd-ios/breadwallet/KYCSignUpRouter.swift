// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCSignUpRoutingLogic {
    var dataStore: KYCSignUpDataStore? { get }
    
    func showKYCConfirmEmailScene()
}

class KYCSignUpRouter: NSObject, KYCSignUpRoutingLogic {
    weak var viewController: KYCSignUpViewController?
    var dataStore: KYCSignUpDataStore?
    
    func showKYCConfirmEmailScene() {
        let kycSignUpViewController = KYCConfirmEmailViewController()
        viewController?.navigationController?.pushViewController(kycSignUpViewController, animated: true)
    }
}
