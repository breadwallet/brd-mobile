//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCConfirmEmailPresentationLogic {
    // MARK: Presentation logic functions
    
    func presentSubmitData(response: KYCConfirmEmail.SubmitData.Response)
    func presentResendCode(response: KYCConfirmEmail.ResendCode.Response)
    func presentShouldEnableConfirm(response: KYCConfirmEmail.ShouldEnableConfirm.Response)
    func presentValidateField(response: KYCConfirmEmail.ValidateField.Response)
    func presentError(response: GenericModels.Error.Response)
}

class KYCConfirmEmailPresenter: KYCConfirmEmailPresentationLogic {
    weak var viewController: KYCConfirmEmailDisplayLogic?
    
    // MARK: Presenter functions
    
    func presentSubmitData(response: KYCConfirmEmail.SubmitData.Response) {
        viewController?.displaySubmitData(viewModel: .init())
    }
    
    func presentResendCode(response: KYCConfirmEmail.ResendCode.Response) {
        viewController?.displayResendCode(viewModel: .init())
    }
    
    func presentShouldEnableConfirm(response: KYCConfirmEmail.ShouldEnableConfirm.Response) {
        viewController?.displayShouldEnableConfirm(viewModel: .init(shouldEnable: response.shouldEnable))
    }
    
    func presentValidateField(response: KYCConfirmEmail.ValidateField.Response) {
        viewController?.displayValidateField(viewModel: .init(isViable: response.isViable))
    }
    
    func presentError(response: GenericModels.Error.Response) {
        viewController?.displayError(viewModel: .init(error: response.error?.errorMessage ?? ""))
    }
}
