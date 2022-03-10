//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCConfirmEmailBusinessLogic {
    // MARK: Business logic functions
    
    func executeCheckFieldType(request: KYCConfirmEmail.CheckFieldText.Request)
    func executeConfirmData(request: KYCConfirmEmail.ConfirmData.Request)
}

protocol KYCConfirmEmailDataStore {
    // MARK: Data store
}

class KYCConfirmEmailInteractor: KYCConfirmEmailBusinessLogic, KYCConfirmEmailDataStore {
    var presenter: KYCConfirmEmailPresentationLogic?
    var worker: KYCConfirmEmailWorker?

    // MARK: Interactor functions
    
    var confirmationCode: String?
    
    func executeCheckFieldType(request: KYCConfirmEmail.CheckFieldText.Request) {
        confirmationCode = request.text
        checkCredentials()
    }
    
    private func checkCredentials() {
        var validationValues = [Bool]()
        validationValues.append(!confirmationCode.isNilOrEmpty)
        
        let shouldEnable = !validationValues.contains(false)
        
        presenter?.presentShouldEnableConfirm(response: .init(shouldEnable: shouldEnable))
        presenter?.presentValidateField(response: .init(isViable: shouldEnable))
    }
    
    func executeConfirmData(request: KYCConfirmEmail.ConfirmData.Request) {
        // implement confirm code when BE is ready
    }
}
