//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCConfirmEmailPresentationLogic {
    // MARK: Presentation logic functions
    
    func presentShouldEnableConfirm(response: KYCConfirmEmail.ShouldEnableConfirm.Response)
    func presentValidateField(response: KYCConfirmEmail.ValidateField.Response)
}

class KYCConfirmEmailPresenter: KYCConfirmEmailPresentationLogic {
    weak var viewController: KYCConfirmEmailDisplayLogic?
    
    // MARK: Presenter functions
    
    func presentShouldEnableConfirm(response: KYCConfirmEmail.ShouldEnableConfirm.Response) {
        viewController?.displayShouldEnableConfirm(viewModel: .init(shouldEnable: response.shouldEnable))
    }
    
    func presentValidateField(response: KYCConfirmEmail.ValidateField.Response) {
        viewController?.displayValidateField(viewModel: .init(isViable: response.isViable))
    }
}
