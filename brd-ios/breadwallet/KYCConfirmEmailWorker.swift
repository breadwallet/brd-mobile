// 
// Created by Equaleyes Solutions Ltd
//

import Foundation

struct KYCConfirmEmailWorkerUrlModelData: UrlModelData {
    let code: String
    
    func urlParameters() -> [String] {
        let sessionKey: String = "sessionKey=\(UserDefaults.kycSessionKeyValue)"
        
        return [sessionKey, code]
    }
}

struct KYCConfirmEmailWorkerRequest: RequestModelData {
    func getParameters() -> [String: Any] {
        return [:]
    }
}

struct KYCConfirmEmailWorkerData: RequestModelData, UrlModelData {
    let workerRequest: KYCConfirmEmailWorkerRequest
    let workerUrlModelData: KYCConfirmEmailWorkerUrlModelData
    
    func getParameters() -> [String: Any] {
        return workerRequest.getParameters()
    }
    
    func urlParameters() -> [String] {
        return workerUrlModelData.urlParameters()
    }
}

class KYCConfirmEmailWorker: KYCBasePlainResponseWorker {
    override func getUrl() -> String {
        guard let urlParams = (requestData as? KYCConfirmEmailWorkerData)?.urlParameters() else { return "" }
        print(APIURLHandler.getUrl(KYCAuthEndpoints.confirm, parameters: urlParams))
        return APIURLHandler.getUrl(KYCAuthEndpoints.confirm, parameters: urlParams)
    }
    
    override func getMethod() -> EQHTTPMethod {
        return .post
    }
}
