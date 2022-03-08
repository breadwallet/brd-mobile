// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCSignUpPresentationLogic {
    // MARK: Presentation logic functions
    
    func presentGetDataForPickerView(response: KYCSignUp.GetDataForPickerView.Response)
    func presentSetPickerValue(response: KYCSignUp.SetPickerValue.Response)
    func presentSubmitData(response: KYCSignUp.SubmitData.Response)
    func presentError(response: GenericModels.Error.Response)
}

class KYCSignUpPresenter: KYCSignUpPresentationLogic {
    weak var viewController: KYCSignUpDisplayLogic?
    
    // MARK: Presenter functions
    
    func presentGetDataForPickerView(response: KYCSignUp.GetDataForPickerView.Response) {
        let countryTitleValues = Constants.Countries.names
        let countryCodes = Constants.Countries.codes
        
        switch response.type {
        case .phonePrefix:
            viewController?.displayGetDataForPickerView(viewModel: .init(index: response.index,
                                                                         pickerValues: countryTitleValues,
                                                                         fieldValues: countryCodes,
                                                                         type: response.type))
            
        default:
            break
        }
    }
    
    func presentSetPickerValue(response: KYCSignUp.SetPickerValue.Response) {
        viewController?.displaySetPickerValue(viewModel: .init(viewModel: .init(firstName: nil,
                                                                                lastName: nil,
                                                                                email: nil,
                                                                                phonePrefix: response.phonePrefix,
                                                                                phoneNumber: nil,
                                                                                password: nil)))
    }
    
    func presentSubmitData(response: KYCSignUp.SubmitData.Response) {
        viewController?.displaySubmitData(viewModel: .init())
    }
    
    func presentError(response: GenericModels.Error.Response) {
        viewController?.displayError(viewModel: .init(error: response.error?.errorMessage ?? ""))
    }
}
