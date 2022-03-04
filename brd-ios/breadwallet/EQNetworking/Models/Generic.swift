//
//  EQNetworking
//  Copyright © 2022 Equaleyes Ltd. All rights reserved.
//

import Foundation

public protocol ModelResponse: Codable {
}

public protocol Model {}

extension Array: Model {}

open class ModelMapper<T: ModelResponse, U: Model> {
    required public init() {}
    
    open func getModel(from response: T) -> U? {
        return nil
    }
}

enum GenericModels {
    enum Error {
        struct Response {
            let error: NetworkingError?
        }
        struct ViewModel {
            let error: String
        }
    }
}
