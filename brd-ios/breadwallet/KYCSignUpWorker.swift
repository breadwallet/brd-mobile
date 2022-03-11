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
    let firstName: String?
    let lastName: String?
    let email: String?
    let phone: String?
    let password: String?
    
    func getParameters() -> [String: Any] {
        return [
            "first_name": firstName ?? "",
            "last_name": lastName ?? "",
            "email": email ?? "",
            "phone": phone ?? "",
            "encryptsha512hex_password": password ?? ""
        ]
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

class KYCSignUpWorker: KYCBaseResponseWorker<KYCSessionModelResponse, KYCSessionModel, KYCSessionModelMapper> {
    override func getUrl() -> String {
        return APIURLHandler.getUrl(KYCAuthEndpoints.register)
    }
    
    override func getMethod() -> EQHTTPMethod {
        return .post
    }
}
