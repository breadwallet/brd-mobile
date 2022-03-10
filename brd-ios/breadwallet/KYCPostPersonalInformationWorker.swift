// 
// Created by Equaleyes Solutions Ltd
//

import Foundation

struct KYCPostPersonalInformationWorkerUrlModelData: UrlModelData {
    func urlParameters() -> [String] {
        let sessionKey: String = "sessionKey=\(UserDefaults.kycSessionKeyValue)"
        
        return [sessionKey]
    }
}

struct KYCPostPersonalInformationWorkerRequest: RequestModelData {
    let street: String?
    let city: String?
    let state: String?
    let zip: String?
    let country: String?
    let dateOfBirth: String?
    let taxIdNumber: String?
    
    func getParameters() -> [String: Any] {
        return [
            "street": street ?? "",
            "city": city ?? "",
            "state": state ?? "",
            "zip": zip ?? "",
            "country": country ?? "",
            "date_of_birth": dateOfBirth ?? "",
            "tax_id_number": taxIdNumber ?? ""
        ]
    }
}

struct KYCPostPersonalInformationWorkerData: RequestModelData, UrlModelData {
    let workerRequest: KYCPostPersonalInformationWorkerRequest
    let workerUrlModelData: KYCPostPersonalInformationWorkerUrlModelData
    
    func getParameters() -> [String: Any] {
        return workerRequest.getParameters()
    }
    
    func urlParameters() -> [String] {
        return workerUrlModelData.urlParameters()
    }
}

class KYCPostPersonalInformationWorker: KYCBasePlainResponseWorker {
    override func getUrl() -> String {
        guard let urlParams = (requestData as? KYCPostPersonalInformationWorkerData)?.urlParameters() else { return "" }
        
        return APIURLHandler.getUrl(KYCEndpoints.personalInformation, parameters: urlParams)
    }
    
    override func getMethod() -> EQHTTPMethod {
        return .post
    }
}
