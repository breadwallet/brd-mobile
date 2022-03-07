// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCPersonalInfoPresentationLogic {
    // MARK: Presentation logic functions
    
    func presentGetDataForPickerView(response: KYCPersonalInfo.GetDataForPickerView.Response)
    func presentSetPickerValue(response: KYCPersonalInfo.SetPickerValue.Response)
}

class KYCPersonalInfoPresenter: KYCPersonalInfoPresentationLogic {
    weak var viewController: KYCPersonalInfoDisplayLogic?
    
    // MARK: Presenter functions
    
    func presentGetDataForPickerView(response: KYCPersonalInfo.GetDataForPickerView.Response) {
        switch response.type {
        case .date:
            viewController?.displayGetDataForPickerView(viewModel: .init(date: response.date,
                                                                         type: response.type))
            
        default:
            break
        }
    }
    
    func presentSetPickerValue(response: KYCPersonalInfo.SetPickerValue.Response) {
        viewController?.displaySetPickerValue(viewModel: .init(viewModel: .init(date: response.date,
                                                                                taxIdNumber: nil)))
    }
}
