//
//  EQNetworking
//  Copyright © 2022 Equaleyes Ltd. All rights reserved.
//

import Foundation

open class BasePlainResponseWorker: APICallWorker {
    public typealias Completion = (NetworkingError?) -> Void
    
    open var requestData: RequestModelData?
    
    open var completion: Completion?
    
    open func execute(requestData: RequestModelData? = nil, completion: Completion?) {
        self.requestData = requestData
        self.completion = completion
        execute()
    }
    
    override open func apiCallDidFinish(response: HTTPResponse) {
        completion?(response.error)
    }
    
    override open func getParameters() -> [String: Any] {
        return requestData?.getParameters() ?? [:]
    }
}
