//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol SignInBusinessLogic {
    // MARK: Business logic functions
    
    func login(request: SignIn.LoginData.Request)
}

protocol SignInDataStore {
    // MARK: Data store
}

class SignInInteractor: SignInBusinessLogic, SignInDataStore {
    var presenter: SignInPresentationLogic?
    var worker = SignInWorker()
    
    // MARK: Interactor functions
    func login(request: SignIn.LoginData.Request) {
        let requestData = SignInWorkerRequest(email: request.email, password: request.password)
        worker.execute(requestData: requestData) { [weak self] error in
            guard error == nil else {
                self?.presenter?.presentError(response: .init(error: error))
                return
            }
            
            self?.presenter?.presentSubmitData(response: .init())
        }
    }
}
