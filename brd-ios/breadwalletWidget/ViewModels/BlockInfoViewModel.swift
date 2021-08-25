// 
//  BlockInfoViewModel.swift
//  breadwalletWidgetExtension
//
//  Created by blockexplorer on 21/08/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import Foundation

struct BlockClockViewModel {

    let currentBlock: String
    let blocksToHalvening: String
    let blocksToHalveningDescription: String
    let progress: Float
    let days: String
    let isPlaceholder: Bool

    init(blockInfo: BlockInfo, isPlaceholder: Bool) {
        let formatter = NumberFormatter()
        let blocksDelta = blockInfo.halveningBlock - blockInfo.currentBlock

        currentBlock = formatter.string(
            from: NSNumber(value: blockInfo.currentBlock)
        ) ?? ""
        blocksToHalvening = formatter.string(
            from: NSNumber(value: blocksDelta)
        ) ?? ""
        blocksToHalveningDescription = String(
            format: "Next halving in %d",
            blocksDelta
        )
        progress = 1 - Float(blocksDelta) / Constant.halveningBlockCount
        days = String(
            format: "%d days", blockInfo.secondsLeft / Constant.secondsInDay
        )

        self.isPlaceholder = isPlaceholder
    }
}

// MARK: - Constant

private extension BlockClockViewModel {

    enum Constant {
        static let halveningBlockCount: Float = 210000
        static let secondsInDay: Int = 86400
    }
}

// MARK: - Mock

extension BlockClockViewModel {

    static func mock() -> BlockClockViewModel {
        return .init(blockInfo: .mock(), isPlaceholder: true)
    }
}
