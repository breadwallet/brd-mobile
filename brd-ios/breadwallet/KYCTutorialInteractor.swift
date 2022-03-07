// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCTutorialBusinessLogic {
    // MARK: Business logic functions
    
    func nextTutorial(request: KYCTutorial.HandleTutorialPaging.Request)
    func fetchTutorialPages(request: KYCTutorial.FetchTutorialPages.Request)
}

protocol KYCTutorialDataStore {
    // MARK: Data store
}

class KYCTutorialInteractor: KYCTutorialBusinessLogic, KYCTutorialDataStore {
    var presenter: KYCTutorialPresentationLogic?
    
    // MARK: Interactor functions
    
    func fetchTutorialPages(request: KYCTutorial.FetchTutorialPages.Request) {
        presenter?.presentTutorialPages(response: .init())
    }
    
    func nextTutorial(request: KYCTutorial.HandleTutorialPaging.Request) {
        let next = request.row + 1
        let finishOnboarding = next != request.pageCount
        
        guard finishOnboarding else {
            return
        }
        
        presenter?.presentNextTutorial(response: .init(nextPage: next))
    }
}
