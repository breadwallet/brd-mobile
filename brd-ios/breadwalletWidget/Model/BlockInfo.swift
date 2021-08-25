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
                currentReward: 1250000000,
                halveningBlock: 943509,
                halveningReward: 6750000000,
                halveningTime: Date().addingTimeInterval(100000),
                secondsLeft: 10000000,
                blocksLeft: 300000
        )
    }
}

