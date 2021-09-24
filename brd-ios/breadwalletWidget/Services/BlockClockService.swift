// 
//  BlockClockService.swift
//  breadwallet
//
//  Created by blockexplorer on 17/08/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import Foundation

typealias BlockInfoHandler = (Result<BlockInfo, Error>) -> Void

protocol BlockClockService {

    func fetchBlockInfo(_ handler: @escaping BlockInfoHandler)
}

struct DefaultBlockClockService: BlockClockService {

    private let decoder: JSONDecoder = {
        let decoder = JSONDecoder()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        dateFormatter.timeZone = TimeZone(identifier: "UTC")
        decoder.dateDecodingStrategy = .formatted(dateFormatter)
        return decoder
    }()

    func fetchBlockInfo(_ handler: @escaping BlockInfoHandler) {
        URLSession.shared.dataTask(
            with: Constant.blockInfoURL,
            completionHandler: { data, _, error in
                guard let data = data else {
                    handler(.failure(error ?? BlockClockServiceError.noData))
                    return
                }
                handleBlockInfoResponse(data: data, handler: handler)
            }
        ).resume()
    }
}

// MARK: - Response

private extension DefaultBlockClockService {

    struct BlockClockResponse: Decodable {
        let data: [String: BlockInfo]
        var bitcoin: BlockInfo? {
            return data[Constant.bitcoinKey]
        }
    }
    
    func handleBlockInfoResponse(data: Data, handler: BlockInfoHandler) {
        let decoder = self.decoder
        do {
            let resp = try decoder.decode(BlockClockResponse.self, from: data)
            if let info = resp.bitcoin {
                handler(.success(info))
                return
            }
            handler(.failure(BlockClockServiceError.noBitcoinInfo))
        } catch {
            handler(.failure(error))
        }
    }
}

// MARK: - Error

private extension DefaultBlockClockService {

    enum BlockClockServiceError: Error {
        case noData
        case noBitcoinInfo
    }
}

// MARK: - Constant

private extension DefaultBlockClockService {

    enum Constant {
        static let bitcoinKey = "bitcoin"
        static let blockInfoURL = URL(
            string: "https://api.blockchair.com/tools/halvening"
        )!
    }
}

