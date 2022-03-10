//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCSignInBusinessLogic {
    // MARK: Business logic functions
    
    func executeCheckFieldType(request: KYCSignIn.CheckFieldText.Request)
    func executeSignIn(request: KYCSignIn.SubmitData.Request)
}

protocol KYCSignInDataStore {
    // MARK: Data store
    
    var email: String? { get set }
    var password: String? { get set }
    
    var fieldValidationIsAllowed: [KYCSignIn.FieldType: Bool] { get set }
}

class KYCSignInInteractor: KYCSignInBusinessLogic, KYCSignInDataStore {
    var presenter: KYCSignInPresentationLogic?
    
    // MARK: Interactor functions
    
    var email: String?
    var password: String?
    
    var fieldValidationIsAllowed = [KYCSignIn.FieldType: Bool]()
    
    func executeSignIn(request: KYCSignIn.SubmitData.Request) {
        let worker = KYCSignInWorker()
        let workerUrlModelData = KYCSignInWorkerUrlModelData()
        let workerRequest = KYCSignInWorkerRequest(email: email,
                                                   password: password)
        let workerData = KYCSignInWorkerData(workerRequest: workerRequest,
                                             workerUrlModelData: workerUrlModelData)
        
        worker.execute(requestData: workerData) { [weak self] response, error in
            guard let sessionKey = response?.data["sessionKey"]?.value as? String, error == nil else {
                self?.presenter?.presentError(response: .init(error: error))
                return
            }
            
            UserDefaults.kycSessionKeyValue = sessionKey
            
            self?.presenter?.presentSignIn(response: .init())
        }
    }
    
    func executeCheckFieldType(request: KYCSignIn.CheckFieldText.Request) {
        switch request.type {
        case .email:
            email = request.text
            
        case .password:
            password = request.text
            
        }
        
        checkCredentials()
    }
    
    private func checkCredentials() {
        var validationValues = [Bool]()
        validationValues.append(!email.isNilOrEmpty)
        validationValues.append(!password.isNilOrEmpty)
        validationValues.append(validatePasswordUsingRegex())
        validationValues.append(validateEmailUsingRegex())
        validationValues.append(contentsOf: fieldValidationIsAllowed.values)
        
        let shouldEnable = !validationValues.contains(false)
        
        presenter?.presentShouldEnableSubmit(response: .init(shouldEnable: shouldEnable))
    }
    
    private func validatePasswordUsingRegex() -> Bool {
        guard let password = password else { return false }
        
        let numberFormat = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"
        let numberPredicate = NSPredicate(format: "SELF MATCHES %@", numberFormat)
        
        let isViable = numberPredicate.evaluate(with: password)
        
        presenter?.presentValidateField(response: .init(isViable: isViable, type: .password))
        
        return true
    }
    
    private func validateEmailUsingRegex() -> Bool {
        guard !email.isNilOrEmpty else { return false }
        
        let emailFormat = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailFormat)
        
        let isViable = emailPredicate.evaluate(with: email)
        
        presenter?.presentValidateField(response: .init(isViable: isViable, type: .email))
        
        return isViable
    }
}
