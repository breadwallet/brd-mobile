//
//  EQNetworking
//  Copyright © 2022 Equaleyes Ltd. All rights reserved.
//

import Foundation

public protocol NetworkingError: Error {
    var errorMessage: String { get }
    var userInfo: [String: Any]? { get set }
    
    init()
    init(data: Data?)
    
    func firstOccurringError() -> String?
}

extension NetworkingError {
    public init(data: Data?) {
        self.init()
        guard let data = data else { return }
        guard let jsonData = try? JSONSerialization.jsonObject(with: data),
              let dictionary = jsonData as? [String: Any] else {
                  return
              }
        userInfo = dictionary
    }
    
    public func firstOccurringError() -> String? {
        var errorMessage = ""
        
        if let userInfo = userInfo,
           let errorValues = (userInfo["error"] as? [String: String]) {
            
            errorMessage += (errorValues["server_message"] ?? "") + "\n"
        }
        
        if let userInfo = userInfo,
           let errorValues = (userInfo["data"] as? [String: [Any]])?.values,
           let reducedError = errorValues.reduce([], +) as? [[String: String]] {
            
            for error in reducedError {
                errorMessage += (error["parameter"] ?? "") + " " + (error["exception"] ?? "") + "\n"
            }
        }
        
        errorMessage = errorMessage.trimmingCharacters(in: .whitespacesAndNewlines)
        
        return errorMessage.trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

public struct NetworkingGeneralError: NetworkingError {
    public var errorMessage: String {
        return firstOccurringError() ?? ""
    }
    public var userInfo: [String: Any]?
    
    public init() {}
}

public struct NetworkingNotFoundError: NetworkingError {
    public var errorMessage: String {
        return firstOccurringError() ?? ""
    }
    public var userInfo: [String: Any]?
    
    public init() {}
}

public struct NetworkingBadRequestError: NetworkingError {
    public var errorMessage: String {
        return firstOccurringError() ?? ""
    }
    public var userInfo: [String: Any]?
    
    public init() {}
    
    public init(userInfo: [String: Any]) {
        self.userInfo = userInfo
    }
}

public struct NetworkingForbiddenError: NetworkingError {
    public var errorMessage: String {
        return firstOccurringError() ?? ""
    }
    public var userInfo: [String: Any]?
    
    public init() {}
}

public struct NetworkingNoConnectionError: NetworkingError {
    public var errorMessage: String {
        return firstOccurringError() ?? ""
    }
    public var userInfo: [String: Any]?
    
    public init() {}
}

public struct NetworkingCustomError: NetworkingError {
    public var errorMessage: String {
        return firstOccurringError() ?? customError
    }
    
    public var userInfo: [String: Any]?
    
    private let customError: String
    
    public init() {
        customError = ""
    }
    
    public init(message: String) {
        self.customError = message
    }
}

public class NetworkingErrorManager {
    static func getError(from response: HTTPURLResponse?, data: Data?, error: Error?) -> NetworkingError? {
        if let error = error as? URLError, error.code == .notConnectedToInternet {
            return NetworkingNoConnectionError()
        }
        guard let response = response else { return NetworkingGeneralError() }
        guard isErrorStatusCode(response.statusCode) else { return nil }
        
        switch response.statusCode {
        case 400:
            return NetworkingBadRequestError(data: data)
        case 401:
            return NetworkingCustomError(data: data)
        case 403:
            return NetworkingForbiddenError(data: data)
        case 404:
            return NetworkingNotFoundError(data: data)
        default:
            return NetworkingGeneralError(data: data)
        }
    }
    
    static func getImageUploadEncodingError() -> NetworkingError {
        return NetworkingGeneralError()
    }
    
    static fileprivate func isErrorStatusCode(_ statusCode: Int) -> Bool {
        if case 400 ... 599 = statusCode {
            return true
        }
        return false
    }
}
