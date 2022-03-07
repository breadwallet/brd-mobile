//
//  EQNetworking
//  Copyright © 2022 Equaleyes Ltd. All rights reserved.
//

import Foundation

class HTTPRequest {
    var method: EQHTTPMethod = .get
    var url = ""
    var headers: [String: String] = [:]
    var parameters: [String: Any] = [:]
    var encoding: Encoding = .json
    
    var media: [MultiPart] = []
    
    init(method: EQHTTPMethod, url: String, headers: [String: String] = [:],
         parameters: [String: Any] = [:], encoding: Encoding = .json) {
        self.method = method
        self.url = url
        self.headers = headers
        self.parameters = parameters
        self.encoding = encoding
        
        // Ignoring using caching respones
        URLSessionConfiguration.default.requestCachePolicy = .reloadIgnoringCacheData
    }
    
    func add(media: [MultiPart]) {
        self.media = media
    }
    
    func runMultipartRequest(completion: @escaping ((HTTPResponse) -> Void)) {
        guard let url = URL(string: url) else { return }
        
        let boundary = "Boundary-\(UUID().uuidString)"
        
        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        
        headers.forEach { request.addValue($0.value, forHTTPHeaderField: $0.key) }
        
        request.addValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let dataBody = createMultipartDataBody(media: media, boundary: boundary)
        request.httpBody = dataBody
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            let httpResponse = self.getHttpResponse(with: request,
                                                    from: response as? HTTPURLResponse,
                                                    data: data,
                                                    error: error)
            completion(httpResponse)
        }.resume()
    }
    
    func createMultipartDataBody(media: [MultiPart], boundary: String) -> Data {
        var body = Data()
        
        let lineBreak = "\r\n"
        
        for mediaFile in media {
            body.append("--\(boundary + lineBreak)")
            
            var str = "Content-Disposition: form-data; "
            if mediaFile.key.isEmpty == false {
                str.append("name=\"\(mediaFile.key)\"; ")
            }
            if let fileName = mediaFile.fileName, fileName.isEmpty == false, mediaFile.mimeFileFormat.value.isEmpty == false {
                str.append("filename=\"\(fileName + mediaFile.mimeFileFormat.value)\"")
            }
            
            body.append("\(str)\(lineBreak)")
            
            body.append("Content-Type: \(mediaFile.mimeType.value + lineBreak + lineBreak)")
            body.append(mediaFile.data)
            body.append(lineBreak)
        }
        
        body.append("--\(boundary)--\(lineBreak)")
        
        return body
    }
    
    func run(completion: @escaping ((HTTPResponse) -> Void)) {
        guard let url = URL(string: url) else { return }
        
        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        
        let jsonData = try? JSONSerialization.data(withJSONObject: parameters,
                                                   options: .prettyPrinted)
        switch method {
        case .get:
            break
        default:
            request.httpBody = jsonData
        }
        
        headers.forEach { request.addValue($0.value, forHTTPHeaderField: $0.key) }
        
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            let httpResponse = self.getHttpResponse(with: request,
                                                    from: response as? HTTPURLResponse,
                                                    data: data,
                                                    error: error)
            completion(httpResponse)
        }.resume()
    }
    
    private func getHttpResponse(with request: URLRequest, from response: HTTPURLResponse?, data: Data?, error: Error?) -> HTTPResponse {
        guard let response = response else { return HTTPResponse() }
        
        var httpResponse = HTTPResponse()
        httpResponse.statusCode = response.statusCode
        httpResponse.request = request
        httpResponse.response = response
        httpResponse.data = data
        
        if let data = data, let responseString = String(data: data, encoding: .utf8) {
            httpResponse.responseValue = try? JSONSerialization.jsonObject(with: data, options: .allowFragments)
            httpResponse.responseString = responseString
        }
        
        httpResponse.error = NetworkingErrorManager.getError(from: response,
                                                             data: data,
                                                             error: error)
        
        if let error = httpResponse.error {
            print("--------    HTTP ERROR:    --------")
            print(httpResponse.request?.url?.absoluteString ?? "")
            print("Status code: \(httpResponse.statusCode)")
            print(error)
            print(httpResponse.responseString)
            print("-----------------------------------")
        }
        
        return httpResponse
    }
}
