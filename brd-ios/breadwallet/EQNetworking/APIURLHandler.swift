//
//  EQNetworking
//  Copyright © 2022 Equaleyes Ltd. All rights reserved.
//

import Foundation

public protocol URLType {
    static var baseURL: String { get }
    var url: String { get }
}

open class APIURLHandler {
    static public func getUrl(_ api: URLType, parameters: [String]) -> String {
        let url = addParameters(to: api.url, parameters: parameters)
        let encodedUrl = encode(url)
        
        return encodedUrl
    }
    
    static public func getUrl(_ api: URLType, parameters: String...) -> String {
        return getUrl(api, parameters: parameters)
    }
    
    private static func addParameters(to url: String, parameters: [String]) -> String {
        return withVaList(parameters) { NSString(format: url, arguments: $0) as String }
    }
    
    private static func encode(_ url: String) -> String {
        return url.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
    }
}
