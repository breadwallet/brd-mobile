//
// Created by Equaleyes Solutions Ltd
//

import UIKit

enum KYCConfirmEmail {
    // MARK: Model name declarations
    
    enum CheckFieldText {
        struct Request {
            let text: String?
        }
    }
    
    enum ShouldEnableConfirm {
        struct Response {
            let shouldEnable: Bool
        }
        struct ViewModel {
            let shouldEnable: Bool
        }
    }
    
    enum ConfirmData {
        struct Request {}
        struct Response {}
        struct ViewModel {}
    }
    
    enum ValidateField {
        struct Response {
            let isViable: Bool
        }
        
        struct ViewModel {
            let isViable: Bool
        }
    }
}
