//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCConfirmEmailRoutingLogic {
    var dataStore: KYCConfirmEmailDataStore? { get }
    
    func showKYCSignInScene()
}

class KYCConfirmEmailRouter: NSObject, KYCConfirmEmailRoutingLogic {
    weak var viewController: KYCConfirmEmailViewController?
    var dataStore: KYCConfirmEmailDataStore?
    
    func showKYCSignInScene() {
        let kycSignInViewController = KYCSignInViewController()
        viewController?.navigationController?.pushViewController(kycSignInViewController, animated: true)
    }
}
