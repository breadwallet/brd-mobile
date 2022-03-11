//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCSignInRoutingLogic {
    var dataStore: KYCSignInDataStore? { get }
    
    func showKYCSignUpScene()
    func showKYCTutorialScene()
    func dismissFlow()
}

class KYCSignInRouter: NSObject, KYCSignInRoutingLogic {
    weak var viewController: KYCSignInViewController?
    var dataStore: KYCSignInDataStore?
    
    func showKYCSignUpScene() {
        let kycSignUpViewController = KYCSignUpViewController()
        viewController?.navigationController?.pushViewController(kycSignUpViewController, animated: true)
    }
    
    func showKYCTutorialScene() {
        let kycTutorialViewController = KYCTutorialViewController()
        viewController?.navigationController?.pushViewController(kycTutorialViewController, animated: true)
    }
    
    func dismissFlow() {
        viewController?.navigationController?.dismiss(animated: true)
    }
}
