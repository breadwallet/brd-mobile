// 
//  BlockClockWidget.swift
//  breadwalletWidgetExtension
//
//  Created by blockexplorer on 17/08/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//


import WidgetKit
import SwiftUI
import Intents

struct BlockClockProvider: IntentTimelineProvider {
    
    typealias Entry = BlockClockEntry
    typealias Intent = BlockClockIntent
    
    let service: BlockClockService = DefaultBlockClockService()
    
    func placeholder(in context: Context) -> BlockClockEntry {
        BlockClockEntry(blockInfo: .mock(), intent: BlockClockIntent(), isPlaceholder: true)
    }

    func getSnapshot(for configuration: BlockClockIntent, in context: Context, completion: @escaping (BlockClockEntry) -> Void) {
        service.fetchBlockInfo { result in
            switch result {
            case let .success(blockInfo):
                DispatchQueue.main.async {
                    completion(
                        BlockClockEntry(blockInfo: blockInfo, intent: configuration)
                    )
                }
            case let .failure(error):
                print(error)
                completion(placeholder(in: context))
            }
        }
    }

    func getTimeline(for configuration: BlockClockIntent, in context: Context, completion: @escaping (Timeline<Entry>) -> Void) {
        getSnapshot(for: configuration, in: context) { entry in
            let timeline = Timeline(entries: [entry],
                policy: .after(Date().adding(minutes: 30)))
            completion(timeline)
        }
    }
}

struct BlockClockEntry: TimelineEntry {
    let date: Date
    let blockInfo: BlockInfo
    let intent: BlockClockIntent
    let viewModel: BlockClockViewModel

    init(blockInfo: BlockInfo, intent: BlockClockIntent, isPlaceholder: Bool = false) {
        self.date = Date()
        self.blockInfo = blockInfo
        self.intent = intent
        self.viewModel = BlockClockViewModel(
            blockInfo: blockInfo,
            isPlaceholder: isPlaceholder
        )
    }
}

struct BlockClockWidgetEntryView: View {
    var entry: BlockClockProvider.Entry

    var body: some View {
        if entry.viewModel.isPlaceholder {
            if entry.intent.style == .default {
                BlockClockView(viewModel: entry.viewModel)
                    .redacted(reason: .placeholder)
            } else {
                BlockClockViewAlt(viewModel: entry.viewModel)
                    .redacted(reason: .placeholder)
            }
        } else {
            if entry.intent.style == .default {
                BlockClockView(viewModel: entry.viewModel)
            } else {
                BlockClockViewAlt(viewModel: entry.viewModel)
            }
        }
    }
}

struct BlockClockWidget: Widget {
    let kind: String = "\(BlockClockWidget.self)"

    var body: some WidgetConfiguration {
        IntentConfiguration(
            kind: kind,
            intent: BlockClockIntent.self,
            provider: BlockClockProvider()
        ) { entry in
            BlockClockWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Block clock")
        .description("Block clock to next halving")
        .supportedFamilies([.systemSmall])
    }
}

struct BlockClockWidget_Previews: PreviewProvider {
    static var previews: some View {

        let entry = BlockClockEntry(blockInfo: .mock(), intent: BlockClockIntent())

        BlockClockWidgetEntryView(entry: entry)
            .previewContext(WidgetPreviewContext(family: .systemSmall))
    }
}
