//
// Created by Equaleyes Solutions Ltd
//

import UIKit

enum KYCSignIn {
    // MARK: Model name declarations
    
    enum FieldType: Codable {
        case email
        case password
    }
    
    enum CheckFieldText {
        struct Request {
            let text: String?
            let type: KYCSignIn.FieldType
        }
    }
    
    enum ValidateField {
        struct Response {
            let isViable: Bool
            let type: KYCSignIn.FieldType
        }
        
        struct ViewModel {
            let isViable: Bool
            let type: KYCSignIn.FieldType
        }
    }
    
    enum SubmitData {
        struct Request {}
        struct Response {}
        struct ViewModel {}
    }
    
    enum ShouldEnableSubmit {
        struct Response {
            let shouldEnable: Bool
        }
        struct ViewModel {
            let shouldEnable: Bool
        }
    }
}
