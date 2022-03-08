//
// Created by Equaleyes Solutions Ltd
//

import UIKit

class SignInWorker: KYCBasePlainResponseWorker {
    override func getUrl() -> String {
        return APIURLHandler.getUrl(KYCLoginEndpoints.login, parameters: [])
    }
    
    override func getMethod() -> EQHTTPMethod {
        return .post
    }
}

struct SignInWorkerData: RequestModelData, UrlModelData {
    let workerRequest: SignInWorkerRequest
    let workerUrlModelData: KYCPostPersonalInformationWorkerUrlModelData
    
    func getParameters() -> [String: Any] {
        return workerRequest.getParameters()
    }
    
    func urlParameters() -> [String] {
        return []
    }
}

struct SignInWorkerRequest: RequestModelData {
    func getParameters() -> [String: Any] {
        return [
            "username": email ?? "",
            "password": password ?? ""
        ]
    }
    
    let email: String?
    let password: String?
}
