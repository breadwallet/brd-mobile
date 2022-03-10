// 
// Created by Equaleyes Solutions Ltd
//

import Foundation

class KYCSessionModelMapper: ModelMapper<KYCSessionModelResponse, KYCSessionModel> {
    override func getModel(from response: KYCSessionModelResponse) -> KYCSessionModel {
        return KYCSessionModel(data: response.data)
    }
}
