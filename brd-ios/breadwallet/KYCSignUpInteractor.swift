// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCSignUpBusinessLogic {
    // MARK: Business logic functions
    
    func executeGetDataForPickerView(request: KYCSignUp.GetDataForPickerView.Request)
    func executeCheckFieldPickerIndex(request: KYCSignUp.CheckFieldPickerIndex.Request)
    func executeCheckFieldType(request: KYCSignUp.CheckFieldText.Request)
    func executeCheckTickBox(request: KYCSignUp.CheckTickBox.Request)
    func executeSubmitData(request: KYCSignUp.SubmitData.Request)
}

protocol KYCSignUpDataStore {
    // MARK: Data store
    
    var firstName: String? { get set }
    var lastName: String? { get set }
    var email: String? { get set }
    var phonePrefix: String? { get set }
    var phoneNumber: String? { get set }
    var password: String? { get set }
    var tickBox: Bool { get set }
    
    var phonePrefixSelectedIndex: PickerViewViewController.Index? { get set }
    var phonePrefixName: String? { get set }
    
    var fieldValidationIsAllowed: [KYCSignUp.FieldType: Bool] { get set }
}

class KYCSignUpInteractor: KYCSignUpBusinessLogic, KYCSignUpDataStore {
    var presenter: KYCSignUpPresentationLogic?
    
    // MARK: Interactor functions
    
    var firstName: String?
    var lastName: String?
    var email: String?
    var phonePrefix: String?
    var phoneNumber: String?
    var password: String?
    var tickBox: Bool
    
    var phonePrefixSelectedIndex: PickerViewViewController.Index?
    var phonePrefixName: String?
    
    var fieldValidationIsAllowed = [KYCSignUp.FieldType: Bool]()
    
    func executeSubmitData(request: KYCSignUp.SubmitData.Request) {
//        let worker = KYCPostPersonalInformationWorker()
//        let workerUrlModelData = KYCPostPersonalInformationWorkerUrlModelData()
//        let workerRequest = KYCPostPersonalInformationWorkerRequest(street: (address ?? "") + " " + (apartment ?? ""),
//                                                                    city: city,
//                                                                    state: state,
//                                                                    zip: zipCode,
//                                                                    country: country,
//                                                                    dateOfBirth: dateOfBirth,
//                                                                    taxIdNumber: taxIdNumber)
//        let workerData = KYCPostPersonalInformationWorkerData(workerRequest: workerRequest,
//                                                              workerUrlModelData: workerUrlModelData)
//
//        worker.execute(requestData: workerData) { [weak self] error in
//            guard error == nil else {
//                self?.presenter?.presentError(response: .init(error: error))
//                return
//            }
//
//            self?.presenter?.presentSubmitData(response: .init())
//        }
    }
    
    func executeGetDataForPickerView(request: KYCSignUp.GetDataForPickerView.Request) {
        switch request.type {
        case .phonePrefix:
            presenter?.presentGetDataForPickerView(response: .init(index: phonePrefixSelectedIndex,
                                                                   type: request.type))
            
        default:
            break
            
        }
    }
    
    func executeCheckFieldPickerIndex(request: KYCSignUp.CheckFieldPickerIndex.Request) {
        let index = request.index
        let fieldValues = request.fieldValues
        let pickerValues = request.pickerValues
        
        switch request.type {
        case .phonePrefix:
            phonePrefix = index == nil ? nil : fieldValues[index?.row ?? 0]
            phonePrefixName = index == nil ? nil : pickerValues[index?.row ?? 0]
            phonePrefixSelectedIndex = index
            
        default:
            break
        }
        
        presenter?.presentSetPickerValue(response: .init(phonePrefix: phonePrefixName ?? ""))
        
        fieldValidationIsAllowed[request.type] = index != nil
        
        checkCredentials()
    }
    
    func executeCheckFieldType(request: KYCSignUp.CheckFieldText.Request) {
        switch request.type {
        case .firstName:
            firstName = request.text
            
        case .lastName:
            lastName = request.text
            
        case .email:
            email = request.text
            
        case .phoneNumber:
            phoneNumber = request.text
            
        case .password:
            password = request.text
            
        default:
            break
        }
        
        checkCredentials()
    }
    
    func executeCheckTickBox(request: KYCSignUp.CheckTickBox.Request) {
        switch request.type {
        case .tickBox:
            tickBox = request.tickBox
            
        default:
            break
        }
        
        checkCredentials()
    }
    
    private func checkCredentials() {
        // TODO: - Implement.....
    }
}
