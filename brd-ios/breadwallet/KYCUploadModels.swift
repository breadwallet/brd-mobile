// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

enum KYCUpload {
    // MARK: Model name declarations
    
    enum SaveImages {
        enum AssetType {
            case selfie
            case frontAndBack
        }
        
        struct Request {
            let type: AssetType
            let images: [UIImage]
        }
        struct Response {}
        struct ViewModel {}
    }
}
