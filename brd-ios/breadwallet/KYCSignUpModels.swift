// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

enum KYCSignUp {
    // MARK: Model name declarations
    
    enum FieldType: Codable {
        case firstName
        case lastName
        case email
        case phonePrefix
        case phoneNumber
        case password
        case tickBox
    }
    
    enum GetDataForPickerView {
        struct Request {
            let type: KYCSignUp.FieldType
        }
        struct Response {
            let index: PickerViewViewController.Index?
            let type: KYCSignUp.FieldType
        }
        struct ViewModel {
            let index: PickerViewViewController.Index?
            let pickerValues: [String]
            let fieldValues: [String]
            let type: KYCSignUp.FieldType
        }
    }
    
    enum CheckFieldPickerIndex {
        struct Request {
            let index: PickerViewViewController.Index?
            let pickerValues: [String]
            let fieldValues: [String]
            let type: KYCSignUp.FieldType
        }
    }
    
    enum SetPickerValue {
        struct Response {
            let phonePrefix: String
        }
        struct ViewModel {
            let viewModel: KYCSignUpCell.ViewModel
        }
    }
    
    enum CheckFieldText {
        struct Request {
            let text: String?
            let type: KYCSignUp.FieldType
        }
    }
    
    enum CheckTickBox {
        struct Request {
            let tickBox: Bool
            let type: KYCSignUp.FieldType
        }
    }
    
    enum SubmitData {
        struct Request {}
        struct Response {}
        struct ViewModel {}
    }
}
