// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

enum KYCAddress {
    // MARK: Model name declarations
    
    enum FieldType: Codable {
        case country
        case zipCode
        case address
        case apartment
        case city
        case state
    }
    
    enum GetDataForPickerView {
        struct Request {
            let type: KYCAddress.FieldType
        }
        struct Response {
            let index: PickerViewViewController.Index?
            let type: KYCAddress.FieldType
        }
        struct ViewModel {
            let index: PickerViewViewController.Index?
            let pickerValues: [String]
            let fieldValues: [String]
            let type: KYCAddress.FieldType
        }
    }
    
    enum CheckFieldPickerIndex {
        struct Request {
            let index: PickerViewViewController.Index?
            let pickerValues: [String]
            let fieldValues: [String]
            let type: KYCAddress.FieldType
        }
    }
    
    enum SetPickerValue {
        struct Response {
            let country: String
            let state: String
        }
        struct ViewModel {
            let viewModel: KYCAddressFieldsCell.ViewModel
        }
    }
    
    enum CheckFieldText {
        struct Request {
            let text: String?
            let type: KYCAddress.FieldType
        }
    }
    
    enum SubmitData {
        struct Request {}
        struct Response {}
        struct ViewModel {}
    }
}
