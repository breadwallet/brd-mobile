//
//  EQNetworking
//  Copyright © 2022 Equaleyes Ltd. All rights reserved.
//

import Foundation

public protocol APICallWorkerProperties {
    func execute()
    func executeMultipartRequest(data: [MultiPart])
    func processResponse(response: HTTPResponse)
    func apiCallDidFinish(response: HTTPResponse)
    func getUrl() -> String
    func getMethod() -> EQHTTPMethod
    func getParameters() -> [String: Any]
    func getHeaders() -> [String: String]
}
/**
 Super class for all workers that make api calls to API
 */
open class APICallWorker {
    var httpRequestManager = HTTPRequestManager()
    
    public init() {}
    
    open func execute() {
        let method = getMethod()
        let url = getUrl()
        let headers = getHeaders()
        var parameters = getParameters()
        
        let request = httpRequestManager.request(method, url: url, headers: headers, parameters: parameters)
        request.run { response in
            DispatchQueue.global(qos: .background).async {
                self.processResponse(response: response)
                DispatchQueue.main.async {
                    self.apiCallDidFinish(response: response)
                }
            }
        }
    }
    
    open func executeMultipartRequest(data: [MultiPart]) {
        let method = getMethod()
        let url = getUrl()
        let headers = getHeaders()
        let parameters = getParameters()
        
        let request = httpRequestManager.request(method, url: url, headers: headers, parameters: parameters)
        request.media = data
        request.runMultipartRequest { response in
            DispatchQueue.global(qos: .background).async {
                self.processResponse(response: response)
                DispatchQueue.main.async {
                    self.apiCallDidFinish(response: response)
                }
            }
        }
    }
    
    /**
     Function for any complex processing of http response. This function is called on a background thread.
     */
    open func processResponse(response: HTTPResponse) { }
    
    /**
     Call completion from this function. This function is called on the main thread.
     */
    open func apiCallDidFinish(response: HTTPResponse) { }
    open func getUrl() -> String { return "" }
    open func getMethod() -> EQHTTPMethod { return .get }
    open func getParameters() -> [String: Any] { return [:] }
    open func getHeaders() -> [String: String] { return [:] }
}
