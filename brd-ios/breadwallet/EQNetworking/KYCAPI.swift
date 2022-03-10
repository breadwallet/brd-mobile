// 
// Created by Equaleyes Solutions Ltd
//

import Foundation

enum KYCEndpoints: String, URLType {
    static var baseURL: String = "https://" + E.apiUrl + "blocksatoshi/one/kyc/%@"
    
    case personalInformation = "pi?%@"
    case uploadSelfieImage = "upload?type=SELFIE%@"
    case uploadFrontBackImage = "upload?type=ID%@"
    case login = "auth/login%@"
    
    var url: String {
        return String(format: Self.baseURL, rawValue)
    }
}

enum KYCAuthEndpoints: String, URLType {
    static var baseURL: String = "https://"  + E.apiUrl + "blocksatoshi/one/auth/%@"
    
    case register
    case login
    case confirm = "register/confirm?%@&confirmation_code=%@"
    case resend = "register/confirm/resend?%@"
    
    var url: String {
        return String(format: Self.baseURL, rawValue)
    }
}

class KYCBaseResponseWorker<T: ModelResponse, U: Model, V: ModelMapper<T, U>>: BaseResponseWorker<T, U, V> {
    
}

class KYCBasePlainResponseWorker: BasePlainResponseWorker {
    
}
