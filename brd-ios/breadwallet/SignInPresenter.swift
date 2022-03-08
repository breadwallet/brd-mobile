//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol SignInPresentationLogic {
    // MARK: Presentation logic functions
    
    func presentSubmitData(response: SignIn.LoginData.Response)
    func presentError(response: GenericModels.Error.Response)
}

class SignInPresenter: SignInPresentationLogic {
    weak var viewController: SignInDisplayLogic?
    
    // MARK: Presenter functions
    
    func presentError(response: GenericModels.Error.Response) {
        viewController?.displayError(viewModel: .init(error: response.error?.errorMessage ?? ""))
    }
    
    func presentSubmitData(response: SignIn.LoginData.Response) {
        viewController?.displayLogin(viewModel: .init())
    }
}
