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

// MARK: - BlockClockView

struct BlockClockView: View {

    @State var viewModel: BlockClockViewModel

    init(viewModel: BlockClockViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        ZStack {
            Color(UIColor.primaryBackground)
                    .ignoresSafeArea()
            VStack(alignment: .leading) {
                HStack {
                    Image("btc")
                        .frame(width: 20, height: 20, alignment: .center)
                    Text("BTC Block")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.orange)
                }
                Spacer()
                Text(viewModel.currentBlock)
                    .font(.system(size: 35, weight: .bold, design: .default))
                    .foregroundColor(.white)
                    .minimumScaleFactor(0.6)
                    .scaledToFit()
                Spacer()
                VStack {
                    HStack {
                        Text("Next halving")
                            .font(.system(size: 12))
                            .foregroundColor(Color(UIColor.textSecondary))
                            .minimumScaleFactor(0.9)
                            .scaledToFit()
                        Spacer()
                        Text(viewModel.blocksToHalvening)
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(Color(UIColor.brdGreen))
                            .minimumScaleFactor(0.9)
                            .scaledToFit()
                    }
                    ProgressView(
                            progress: CGFloat(viewModel.progress),
                            filledColor: Color(UIColor.brdGreen)
                    )
                    .frame(maxHeight: 5)
                }
                HStack(alignment: .center) {
                    Spacer()
                    Text(viewModel.days)
                        .font(.system(size: 8))
                        .foregroundColor(.gray)
                    Spacer()
                }
            }.padding()
        }
    }
}

// MARK: - BlockClockViewAlt

struct BlockClockViewAlt: View {

    @State var viewModel: BlockClockViewModel

    init(viewModel: BlockClockViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        ZStack {
            Color(UIColor.primaryBackground)
                .ignoresSafeArea()
            VStack(alignment: .center) {
                Image("btc")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 35, height: 35)
                Spacer()
                Text(viewModel.currentBlock)
                    .font(.system(size: 35, weight: .bold, design: .default))
                    .foregroundColor(.white)
                    .minimumScaleFactor(0.6)
                    .scaledToFit()
                Spacer()
                VStack(spacing: 4) {
                    Text(viewModel.blocksToHalveningDescription)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(Color(UIColor.gradientSuccessBRD))
                        .minimumScaleFactor(0.5)
                        .scaledToFit()
                    Text(viewModel.days)
                        .font(.system(size: 8))
                        .foregroundColor(Color(UIColor.textSecondary))
                }
            }.padding()
        }
    }
}


struct ProgressView: View {
    var progress: CGFloat
    var bgColor = Color.black.opacity(0.2)
    var filledColor = Color.blue

    var body: some View {
        GeometryReader { geometry in
            let height = geometry.size.height
            let width = geometry.size.width
            ZStack(alignment: .leading) {
                Rectangle()
                    .foregroundColor(bgColor)
                    .frame(width: width,
                            height: height)
                    .cornerRadius(height / 2.0)
                Rectangle()
                    .foregroundColor(filledColor)
                    .frame(width: width * self.progress,
                            height: height)
                    .cornerRadius(height / 2.0)
            }
        }
    }
}
