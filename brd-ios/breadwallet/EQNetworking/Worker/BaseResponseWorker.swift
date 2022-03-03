//
//  EQNetworking
//  Copyright © 2022 Equaleyes Ltd. All rights reserved.
//

import Foundation

open class BaseResponseWorker<T: ModelResponse, U: Model, V: ModelMapper<T, U>>: APICallWorker {
    public typealias Completion = (U?, NetworkingError?) -> Void
    
    open var requestData: RequestModelData?
    
    open var completion: Completion?
    open var result: U?
    
    open func execute(requestData: RequestModelData? = nil, completion: Completion?) {
        self.requestData = requestData
        self.completion = completion
        execute()
    }
    
    override open func processResponse(response: HTTPResponse) {
        guard let data = response.data, response.error == nil else { return }
        guard let payload = T.parse(from: data, type: T.self) else { return }
        let mapper = V()
        result = mapper.getModel(from: payload)
    }
    
    override open func apiCallDidFinish(response: HTTPResponse) {
        completion?(result, response.error)
    }
    
    override open func getParameters() -> [String: Any] {
        return requestData?.getParameters() ?? [:]
    }
}
