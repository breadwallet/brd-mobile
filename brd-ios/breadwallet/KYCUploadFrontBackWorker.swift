// 
// Created by Equaleyes Solutions Ltd
//

import Foundation

struct KYCUploadFrontBackWorkerUrlModelData: UrlModelData {
    func urlParameters() -> [String] {
        let sessionKey: String = "sessionKey=\(UserDefaults.kycSessionKeyValue)"
        
        return [sessionKey]
    }
}

struct KYCUploadFrontBackWorkerRequest: RequestModelData {
    let imageData: [Data]
    
    func getParameters() -> [String: Any] {
        let value = [KYCUploadFrontBackWorker.backKey: imageData[1],
                     KYCUploadFrontBackWorker.frontKey: imageData[0]]
        
        return value
    }
}

struct KYCUploadFrontBackWorkerData: RequestModelData, UrlModelData {
    let workerRequest: KYCUploadFrontBackWorkerRequest
    let workerUrlModelData: KYCUploadFrontBackWorkerUrlModelData
    
    func getParameters() -> [String: Any] {
        return workerRequest.getParameters()
    }
    
    func urlParameters() -> [String] {
        return workerUrlModelData.urlParameters()
    }
}

class KYCUploadFrontBackWorker: KYCBasePlainResponseWorker {
    static let frontKey: String = "auto_upload_file"
    static let backKey: String = "auto_upload_file_back"
    
    override func execute() {
        guard let getParameters = (requestData as? KYCUploadFrontBackWorkerData)?.getParameters() else { return }
        
        guard let frontValue = getParameters[KYCUploadFrontBackWorker.frontKey] as? Data,
              let backValue = getParameters[KYCUploadFrontBackWorker.backKey] as? Data else { return }
        
        executeMultipartRequest(data: [MultipartMedia(with: frontValue,
                                                      fileName: UUID().uuidString,
                                                      forKey: KYCUploadFrontBackWorker.frontKey,
                                                      mimeType: .jpeg,
                                                      mimeFileFormat: .jpeg),
                                       MultipartMedia(with: backValue,
                                                      fileName: UUID().uuidString,
                                                      forKey: KYCUploadFrontBackWorker.backKey,
                                                      mimeType: .jpeg,
                                                      mimeFileFormat: .jpeg)])
    }
    
    override func getUrl() -> String {
        guard let urlParams = (requestData as? KYCUploadFrontBackWorkerData)?.urlParameters() else { return "" }
        
        return APIURLHandler.getUrl(KYCEndpoints.uploadFrontBackImage, parameters: urlParams)
    }
    
    override func getMethod() -> EQHTTPMethod {
        return .post
    }
}
