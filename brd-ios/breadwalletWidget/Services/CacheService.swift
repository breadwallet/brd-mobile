//
//  CacheService.swift
//  breadwallet
//
//  Created by stringcode on 21/02/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//
//  See the LICENSE file at the project root for license information.
//

import Foundation

protocol CacheService {
    func cache<T: Encodable>(value: T, key: String)
    func cached<T: Decodable>(key: String) -> T?
}

class DefaultCacheService: CacheService {

    private lazy var encoder = JSONEncoder()
    private lazy var decoder = JSONDecoder()

    func cache<T: Encodable>(value: T, key: String) {
        createFolderIfNeeded()
        FileManager.default.createFile(
            atPath: cacheFolder.appendingPathComponent(key).path,
            contents: try? encoder.encode(value)
        )
    }

    func cached<T: Decodable>(key: String) -> T? {
        clearOldCaches()
        let path = cacheFolder.appendingPathComponent(key)
        guard let data = try? Data(contentsOf: path) else {
            return nil
        }
        return try? decoder.decode(T.self, from: data)
    }
}

// MARK: - Utilities

private extension DefaultCacheService {

    var cacheFolder: URL {
        FileManager.default.urls(
            for: .cachesDirectory,
            in: .userDomainMask
        )[0].appendingPathComponent(Constant.cacheFolder)
    }

    func clearOldCaches() {
        var filesToDelete = [URL]()
        let monthAgo = Date().adding(days: -30)
        let fileManager = FileManager.default
        let enumerator = fileManager.enumerator(
            at: cacheFolder,
            includingPropertiesForKeys: [.attributeModificationDateKey]
        )

        while let file = enumerator?.nextObject() as? String {
            let url = cacheFolder.appendingPathComponent(file)
            let attrs = try? fileManager.attributesOfItem(atPath: url.path)
            if let date = attrs?[.modificationDate] as? Date, date < monthAgo {
                filesToDelete.append(url)
            }
        }

        for file in filesToDelete {
            try? fileManager.removeItem(at: file)
        }
    }

    func createFolderIfNeeded() {
        try? FileManager.default.createDirectory(
            at: cacheFolder,
            withIntermediateDirectories: true
        )
    }
}

// MARK: - Constants

private extension  DefaultCacheService {

    enum Constant {
        static let cacheFolder = "marketdata"
    }
}