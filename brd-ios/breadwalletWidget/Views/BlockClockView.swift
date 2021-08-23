// 
//  BlockClockView.swift
//  breadwalletWidgetExtension
//
//  Created by blockexplorer on 17/08/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import SwiftUI

struct BlockClockView: View {

    private var viewModel: BlockClockViewModel

    init(viewModel: BlockClockViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        Text("BlockClock")
    }
}
