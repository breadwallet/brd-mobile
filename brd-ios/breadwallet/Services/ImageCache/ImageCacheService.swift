//
//  ImageCacheService.swift
//  breadwallet
//
//  Created by blockexplorer on 08/04/2021.
//  Copyright (c) 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

protocol ImageCacheService {

    typealias Handler = (Result<UIImage, Error>) -> Void

    func image(url: URL, handler: @escaping Handler)
    func cancel(_ url: URL?, urlHash: Int?)
}

class DefaultImageCache: ImageCacheService {

    static let shared = DefaultImageCache()

    private let downloadQueue: OperationQueue = {
        var queue = OperationQueue()
        queue.name = "ImageCache Queue"
        queue.maxConcurrentOperationCount = 20
        return queue
    }()

    func image(url: URL, handler: @escaping ImageCacheService.Handler) {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            if let image = self?.cachedImage(for: url) {
                handler(.success(image))
                return
            }

            self?.downloadImage(at: url) { result in
                switch result {
                case let .success(image):
                    handler(.success(image))
                    self?.cache(image, url: url)
                case let .failure(err):
                    handler(.failure(err))
                }
            }
        }
    }

    func cancel(_ url: URL? = nil, urlHash: Int? = nil) {
        guard let id = url?.absoluteString.sdbmhash ?? urlHash else {
            return
        }

        downloadQueue.operations
            .first(where: { ($0 as? ImageDownloadOperation)?.urlHash == id })?
            .cancel()
    }

    private func downloadImage(
        at url: URL,
        handler: @escaping ImageCacheService.Handler
    ) {
        let op = ImageDownloadOperation(url)
        op.completionBlock = { [weak op] in
            guard (op?.isCancelled ?? false) == false else {
                return
            }
            guard let result = op?.result else {
                handler(.failure(ImageCacheError.unknownOperationError))
                return
            }
            handler(result)
        }
        downloadQueue.addOperation(op)
    }

    private func cachedImage(for url: URL) -> UIImage? {
        guard let data = try? Data(contentsOf: cacheURL(url)) else {
            return nil
        }
        return UIImage(data: data, scale: UIScreen.main.scale)
    }

    private func cache(_ image: UIImage, url: URL) {
        try? image.pngData()?.write(to: cacheURL(url))
    }

    private func cachePath(_ url: URL) -> String {
        return NSTemporaryDirectory() + "\(url.absoluteString.sdbmhash).png"
    }

    private func cacheURL(_ url: URL) -> URL {
        return URL(fileURLWithPath: cachePath(url))
    }
}

// MARK: - ImageDownloadOperation

private final class ImageDownloadOperation: Operation {

    let urlHash: Int
    let url: URL

    var result: Result<UIImage, Error>?

    init(_ url: URL) {
        self.url = url
        self.urlHash = url.absoluteString.sdbmhash
    }

    override func main() {
        if isCancelled {
            return
        }

        do {
            let data = try Data(contentsOf: url)
            let scale = UIScreen.main.scale

            if isCancelled {
                return
            }

            guard let image = UIImage(data: data, scale: scale) else {
                result = .failure(ImageCacheError.failedToCreateImageFromData)
                return
            }

            result = .success(image)
        } catch {
            result = .failure(error)
        }
    }
}

// MARK: - Error

enum ImageCacheError: Error {
    case failedToLoadData
    case failedToCreateImageFromData
    case unknownOperationError
}
