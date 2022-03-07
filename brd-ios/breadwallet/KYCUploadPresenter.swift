// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCUploadPresentationLogic {
    // MARK: Presentation logic functions
    
    func presentSaveImage(response: KYCUpload.SaveImages.Response)
    func presentError(response: GenericModels.Error.Response)
}

class KYCUploadPresenter: KYCUploadPresentationLogic {
    weak var viewController: KYCUploadDisplayLogic?
    
    // MARK: Presenter functions
    
    func presentSaveImage(response: KYCUpload.SaveImages.Response) {
        viewController?.displaySaveImage(viewModel: .init())
    }
    
    func presentError(response: GenericModels.Error.Response) {
        viewController?.displayError(viewModel: .init(error: response.error?.errorMessage ?? ""))
    }
}
