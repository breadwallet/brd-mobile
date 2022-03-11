// 
// Created by Equaleyes Solutions Ltd
//

import Foundation

struct KYCResendConfirmEmailWorkerUrlModelData: UrlModelData {
    func urlParameters() -> [String] {
        let sessionKey: String = "sessionKey=\(UserDefaults.kycSessionKeyValue)"
        
        return [sessionKey]
    }
}

struct KYCResendConfirmEmailWorkerRequest: RequestModelData {
    func getParameters() -> [String: Any] {
        return [:]
    }
}

struct KYCResendConfirmEmailWorkerData: RequestModelData, UrlModelData {
    let workerRequest: KYCResendConfirmEmailWorkerRequest
    let workerUrlModelData: KYCResendConfirmEmailWorkerUrlModelData
    
    func getParameters() -> [String: Any] {
        return workerRequest.getParameters()
    }
    
    func urlParameters() -> [String] {
        return workerUrlModelData.urlParameters()
    }
}

class KYCResendConfirmEmailWorker: KYCBasePlainResponseWorker {
    override func getUrl() -> String {
        guard let urlParams = (requestData as? KYCResendConfirmEmailWorkerData)?.urlParameters() else { return "" }
        
        return APIURLHandler.getUrl(KYCAuthEndpoints.resend, parameters: urlParams)
    }
    
    override func getMethod() -> EQHTTPMethod {
        return .post
    }
}
