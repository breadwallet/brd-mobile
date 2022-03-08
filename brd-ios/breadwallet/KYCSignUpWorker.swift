// 
// Created by Equaleyes Solutions Ltd
//

import Foundation

struct KYCSignUpWorkerUrlModelData: UrlModelData {
    func urlParameters() -> [String] {
        return []
    }
}

struct KYCSignUpWorkerRequest: RequestModelData {
    func getParameters() -> [String: Any] {
        return [:]
    }
}

struct KYCSignUpWorkerData: RequestModelData, UrlModelData {
    let workerRequest: KYCSignUpWorkerRequest
    let workerUrlModelData: KYCSignUpWorkerUrlModelData
    
    func getParameters() -> [String: Any] {
        return workerRequest.getParameters()
    }
    
    func urlParameters() -> [String] {
        return workerUrlModelData.urlParameters()
    }
}

class KYCSignUpWorker: KYCBasePlainResponseWorker {
    override func getUrl() -> String {
        guard let urlParams = (requestData as? KYCSignUpWorkerData)?.urlParameters() else { return "" }
        
        return APIURLHandler.getUrl(KYCAuthEndpoints.register, parameters: urlParams)
    }
    
    override func getMethod() -> EQHTTPMethod {
        return .post
    }
}
