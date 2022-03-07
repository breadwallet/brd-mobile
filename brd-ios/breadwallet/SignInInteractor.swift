//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol SignInBusinessLogic {
    // MARK: Business logic functions
}

protocol SignInDataStore {
    // MARK: Data store
}

class SignInInteractor: SignInBusinessLogic, SignInDataStore {
    var presenter: SignInPresentationLogic?
    var worker: SignInWorker?

    // MARK: Interactor functions

}
