//
// Created by Equaleyes Solutions Ltd
//

import UIKit

struct KYCSignInWorkerUrlModelData: UrlModelData {
    func urlParameters() -> [String] {
        return []
    }
}

struct KYCSignInWorkerRequest: RequestModelData {
    let email: String?
    let password: String?
    
    func getParameters() -> [String: Any] {
        return [
            "username": email ?? "",
            "password": password ?? ""
        ]
    }
}

struct KYCSignInWorkerData: RequestModelData, UrlModelData {
    let workerRequest: KYCSignInWorkerRequest
    let workerUrlModelData: KYCSignInWorkerUrlModelData
    
    func getParameters() -> [String: Any] {
        return workerRequest.getParameters()
    }
    
    func urlParameters() -> [String] {
        return workerUrlModelData.urlParameters()
    }
}

class KYCSignInWorker: KYCBaseResponseWorker<KYCSessionModelResponse, KYCSessionModel, KYCSessionModelMapper> {
    override func getUrl() -> String {
        return APIURLHandler.getUrl(KYCAuthEndpoints.login)
    }
    
    override func getMethod() -> EQHTTPMethod {
        return .post
    }
}
