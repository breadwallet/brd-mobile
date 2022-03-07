//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol ChooseRegistrationBusinessLogic {
    // MARK: Business logic functions
}

protocol ChooseRegistrationDataStore {
    // MARK: Data store
}

class ChooseRegistrationInteractor: ChooseRegistrationBusinessLogic, ChooseRegistrationDataStore {
    var presenter: ChooseRegistrationPresentationLogic?
    var worker: ChooseRegistrationWorker?

    // MARK: Interactor functions

}
