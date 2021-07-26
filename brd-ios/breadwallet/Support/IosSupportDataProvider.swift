//
// Created by blockexplorer on 07/07/2021.
// Copyright (c) 2021 Breadwinner AG. All rights reserved.
//

import Foundation
import Cosmos

class IosSupportDataProvider: SupportDataProvider {

    func load(fileName: String) -> String {
        let components: [String] = fileName.split(separator: ".")
            .map { String($0) }

        guard let url = Bundle.main.url(
                forResource: components.first,
                withExtension: components.last
        ) else {
            return "[]"
        }

        return (try? String(contentsOf: url)) ?? "[]"
    }
}
