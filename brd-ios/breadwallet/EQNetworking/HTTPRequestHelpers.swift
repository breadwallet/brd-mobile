//
//  EQNetworking
//  Copyright © 2022 Equaleyes Ltd. All rights reserved.
//

import Foundation

public enum EQHTTPMethod: String {
    case options = "OPTIONS"
    case get = "GET"
    case head = "HEAD"
    case post = "POST"
    case put = "PUT"
    case patch = "PATCH"
    case delete = "DELETE"
    case trace = "TRACE"
    case connect = "CONNECT"
}

enum Encoding {
    // Maybe support "application/x-www-form-urlencoded"
    
    case json
}

public struct HTTPResponse {
    public var statusCode = 0
    public var responseString = ""
    public var responseValue: Any?
    public var error: NetworkingError?
    public var request: URLRequest?
    public var response: HTTPURLResponse?
    public var data: Data?
}

public protocol MultiPart {
    var key: String { get set }
    var fileName: String? { get set }
    var data: Data { get set }
    var mimeType: MultipartMedia.MimeType { get set }
    var mimeFileFormat: MultipartMedia.MimeFileFormat { get set }
}

public struct MultipartMedia: MultiPart {
    public enum MimeType {
        case jpeg
        case png
        case pdf
        case other(type: String)
        
        var value: String {
            switch self {
            case .jpeg:
                return "image/jpeg"
            case .png:
                return "image/png"
            case .pdf:
                return "application/pdf"
            case .other(type: let type):
                return type
            }
        }
    }
    
    public enum MimeFileFormat {
        case jpeg
        case png
        case pdf
        case other(format: String)
        
        var value: String {
            switch self {
            case .jpeg:
                return ".jpeg"
            case .png:
                return ".png"
            case .pdf:
                return ".pdf"
            case .other(format: let format):
                return "." + format
            }
        }
    }
    
    public var key: String
    public var fileName: String?
    public var data: Data
    public var mimeType: MimeType
    public var mimeFileFormat: MimeFileFormat
    
    public init(with data: Data, fileName: String? = nil, forKey key: String, mimeType: MimeType, mimeFileFormat: MimeFileFormat) {
        self.key = key
        self.fileName = fileName
        self.data = data
        self.mimeType = mimeType
        self.mimeFileFormat = mimeFileFormat
    }
}

extension Data {
    mutating func append(_ string: String) {
        if let data = string.data(using: .utf8) {
            append(data)
        }
    }
}
