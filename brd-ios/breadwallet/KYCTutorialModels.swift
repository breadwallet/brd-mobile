// 
// Created by Equaleyes Solutions Ltd
// 

import UIKit

enum KYCTutorial {
    // MARK: Model name declarations
    
    struct DisplayedTutorialPage {
        let topTitle: String
        let icon: UIImage?
        let title: String
        let text: String
        let identifier: String
    }
    
    enum FetchTutorialPages {
        struct Request {}
        struct Response {}
        struct ViewModel {
            let tutorialPagesIdentifiers: [String]
        }
    }
    
    enum HandleTutorialPaging {
        struct Request {
            let row: Int
            let pageCount: Int
        }
        struct Response {
            let nextPage: Int
        }
        struct ViewModel {
            let nextPage: Int
        }
    }
}
