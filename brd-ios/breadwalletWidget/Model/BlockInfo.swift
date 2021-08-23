//
// Created by blockexplorer on 17/08/2021.
// Copyright (c) 2021 Breadwinner AG. All rights reserved.
//

import Foundation

struct BlockInfo {
    let currentBlock: Int
    let currentReward: Int
    let halveningBlock: Int
    let halveningReward: Int
    let halveningTime: Date
    let secondsLeft: Int
    let blocksLeft: Int
}

// MARK: - Decodable

extension BlockInfo: Decodable {

    enum CodingKeys: String, CodingKey {
        case currentBlock = "current_block"
        case currentReward = "current_reward"
        case halveningBlock = "halvening_block"
        case halveningReward = "halvening_reward"
        case halveningTime = "halvening_time"
        case secondsLeft = "seconds_left"
        case blocksLeft = "blocks_left"
    }
}

// MARK: - Mock

extension BlockInfo {
    
    static func mock() -> BlockInfo {
        return .init(
            currentBlock: 643509,
            currentReward: 12.5,
            halveningBlock: 1204983,
            halveningReward: 6.75,
            halveningTime: Date().addingTimeInterval(100000),
            secondsLeft: 100000,
            blocksLeft: 10000
        )
    }
}
