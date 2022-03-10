// 
// Created by Equaleyes Solutions Ltd
//

import AnyCodable
import Foundation

struct KYCSessionModelResponse: Codable, ModelResponse {
    let data: [String: AnyCodable]
}
