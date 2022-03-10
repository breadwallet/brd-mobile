//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCConfirmEmailBusinessLogic {
    // MARK: Business logic functions
    
    func executeCheckFieldType(request: KYCConfirmEmail.CheckFieldText.Request)
    func executeSubmitData(request: KYCConfirmEmail.SubmitData.Request)
    func executeResendCode(request: KYCConfirmEmail.ResendCode.Request)
}

protocol KYCConfirmEmailDataStore {
    // MARK: Data store
    
    var confirmationCode: String? { get set }
}

class KYCConfirmEmailInteractor: KYCConfirmEmailBusinessLogic, KYCConfirmEmailDataStore {
    var presenter: KYCConfirmEmailPresentationLogic?
    
    // MARK: Interactor functions
    
    var confirmationCode: String?
    
    func executeSubmitData(request: KYCConfirmEmail.SubmitData.Request) {
        let worker = KYCConfirmEmailWorker()
        let workerUrlModelData = KYCConfirmEmailWorkerUrlModelData(code: confirmationCode ?? "")
        let workerRequest = KYCConfirmEmailWorkerRequest()
        let workerData = KYCConfirmEmailWorkerData(workerRequest: workerRequest,
                                                   workerUrlModelData: workerUrlModelData)
        
        worker.execute(requestData: workerData) { [weak self] error in
            guard error == nil else {
                self?.presenter?.presentError(response: .init(error: error))
                return
            }
            
            self?.presenter?.presentSubmitData(response: .init())
        }
    }
    
    func executeResendCode(request: KYCConfirmEmail.ResendCode.Request) {
        let worker = KYCResendConfirmEmailWorker()
        let workerUrlModelData = KYCResendConfirmEmailWorkerUrlModelData()
        let workerRequest = KYCResendConfirmEmailWorkerRequest()
        let workerData = KYCResendConfirmEmailWorkerData(workerRequest: workerRequest,
                                                         workerUrlModelData: workerUrlModelData)
        
        worker.execute(requestData: workerData) { [weak self] error in
            guard error == nil else {
                self?.presenter?.presentError(response: .init(error: error))
                return
            }
            
            self?.presenter?.presentResendCode(response: .init())
        }
    }
    
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
}
