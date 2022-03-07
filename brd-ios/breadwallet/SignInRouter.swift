//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol SignInRoutingLogic {
    var dataStore: SignInDataStore? { get }
    func showSignUp()
}

class SignInRouter: NSObject, SignInRoutingLogic {
    weak var viewController: SignInViewController?
    var dataStore: SignInDataStore?
    
    func showSignUp() {
        // open sign up
    }
}
