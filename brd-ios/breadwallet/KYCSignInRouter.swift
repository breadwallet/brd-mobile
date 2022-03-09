//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCSignInRoutingLogic {
    var dataStore: KYCSignInDataStore? { get }
    
    func showKYCSignUpScene()
}

class KYCSignInRouter: NSObject, KYCSignInRoutingLogic {
    weak var viewController: KYCSignInViewController?
    var dataStore: KYCSignInDataStore?
    
    func showKYCSignUpScene() {
        let kycSignUpViewController = KYCSignUpViewController()
        viewController?.navigationController?.pushViewController(kycSignUpViewController, animated: true)
    }
}
