// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCPersonalInfoBusinessLogic {
    // MARK: Business logic functions
    
    func executeGetDataForPickerView(request: KYCPersonalInfo.GetDataForPickerView.Request)
    func executeCheckFieldPickerIndex(request: KYCPersonalInfo.CheckFieldPickerIndex.Request)
    func executeCheckFieldType(request: KYCPersonalInfo.CheckFieldText.Request)
}

protocol KYCPersonalInfoDataStore {
    // MARK: Data store
    
    var date: String? { get set }
    var taxIdNumber: String? { get set }
    
    var selectedCurrentDate: Date? { get set }
    
    var fieldValidationIsAllowed: [KYCPersonalInfo.FieldType: Bool] { get set }
}

class KYCPersonalInfoInteractor: KYCPersonalInfoBusinessLogic, KYCPersonalInfoDataStore {
    var presenter: KYCPersonalInfoPresentationLogic?
    
    // MARK: Interactor functions
    
    var date: String?
    var taxIdNumber: String?
    
    var selectedCurrentDate: Date?
    
    var fieldValidationIsAllowed = [KYCPersonalInfo.FieldType: Bool]()
    
    func executeGetDataForPickerView(request: KYCPersonalInfo.GetDataForPickerView.Request) {
        switch request.type {
        case .date:
            presenter?.presentGetDataForPickerView(response: .init(date: selectedCurrentDate,
                                                                   type: request.type))
            
        default:
            break
            
        }
    }
    
    func executeCheckFieldPickerIndex(request: KYCPersonalInfo.CheckFieldPickerIndex.Request) {
        let selectedDate = request.selectedDate
        
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let formattedDateString = formatter.string(from: selectedDate)
        
        switch request.type {
        case .date:
            date = formattedDateString
            selectedCurrentDate = selectedDate
            
        default:
            break
        }
        
        presenter?.presentSetPickerValue(response: .init(date: date ?? ""))
        
        // TODO: - Implement.....
//        fieldValidationIsAllowed[request.type] = index != nil
        
        checkCredentials()
    }
    
    func executeCheckFieldType(request: KYCPersonalInfo.CheckFieldText.Request) {
        switch request.type {
        case .taxIdNumber:
            taxIdNumber = request.text
            
        default:
            break
        }
        
        checkCredentials()
    }
    
    private func checkCredentials() {
        // TODO: - Implement.....
    }
}
