//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCSignInPresentationLogic {
    // MARK: Presentation logic functions
    
    func presentSignIn(response: KYCSignIn.SubmitData.Response)
    func presentShouldEnableSubmit(response: KYCSignIn.ShouldEnableSubmit.Response)
    func presentValidateField(response: KYCSignIn.ValidateField.Response)
    func presentError(response: GenericModels.Error.Response)
}

class KYCSignInPresenter: KYCSignInPresentationLogic {
    weak var viewController: KYCSignInDisplayLogic?
    
    // MARK: Presenter functions
    
    func presentSignIn(response: KYCSignIn.SubmitData.Response) {
        viewController?.displaySignIn(viewModel: .init())
    }
    
    func presentShouldEnableSubmit(response: KYCSignIn.ShouldEnableSubmit.Response) {
        viewController?.displayShouldEnableSubmit(viewModel: .init(shouldEnable: response.shouldEnable))
    }
    
    func presentValidateField(response: KYCSignIn.ValidateField.Response) {
        viewController?.displayValidateField(viewModel: .init(isViable: response.isViable,
                                                              type: response.type))
    }
    
    func presentError(response: GenericModels.Error.Response) {
        viewController?.displayError(viewModel: .init(error: response.error?.errorMessage ?? ""))
    }
}
