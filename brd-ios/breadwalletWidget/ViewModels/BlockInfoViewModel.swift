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
    
    var isPlaceholder: Bool
    
    private var blockInfo: BlockInfo
    
    init(blockInfo: BlockInfo, isPlaceholder: Bool = false) {
        self.blockInfo = blockInfo
        self.isPlaceholder = isPlaceholder
    }
}

// MARK: - BlockClockViewModel

extension BlockClockViewModel {
    
    static func mock() -> BlockClockViewModel {
        return .init(blockInfo: .mock(), isPlaceholder: true)
    }
}
