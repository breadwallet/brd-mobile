// 
// Created by Equaleyes Solutions Ltd
// 

import UIKit

protocol KYCTutorialPresentationLogic {
    // MARK: Presentation logic functions
    
    func presentTutorialPages(response: KYCTutorial.FetchTutorialPages.Response)
    func presentNextTutorial(response: KYCTutorial.HandleTutorialPaging.Response)
}

class KYCTutorialPresenter: KYCTutorialPresentationLogic {
    weak var viewController: KYCTutorialDisplayLogic?
    
    // MARK: Presenter functions
    
    func presentTutorialPages(response: KYCTutorial.FetchTutorialPages.Response) {
        let identifiers = [String(describing: KYCTutorial1CollectionViewCell.self),
                           String(describing: KYCTutorial2CollectionViewCell.self)]
        viewController?.displayTutorialPages(viewModel: .init(tutorialPagesIdentifiers: identifiers))
    }
    
    func presentNextTutorial(response: KYCTutorial.HandleTutorialPaging.Response) {
        viewController?.displayNextTutorial(viewModel: .init(nextPage: response.nextPage))
    }
}
