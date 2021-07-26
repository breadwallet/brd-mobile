//
//  UIImageView+Cache.swift
//  breadwallet
//
//  Created by blockexplorer on 08/04/2021.
//  Copyright (c) 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

extension UIImageView {

    enum Placeholder {
        case image(UIImage)
        case activityIndicator
        case none
    }

    func setImage(url: URL?, placeholder: Placeholder) {
        let previousTag = tag
        let ogTag = url?.absoluteString.sdbmhash ?? 0

        guard previousTag != ogTag else {
            return
        }

        switch placeholder {
        case let .image(image):
            if previousTag != ogTag {
                self.image = image
                removeActivityIndicator()
            }
        case .activityIndicator:
            if previousTag != ogTag {
                addActivityIndicator()
            }
        case .none:
            if previousTag != ogTag {
                image = nil
                addActivityIndicator()
            }
        }

        guard let url = url else {
            return
        }

        tag = ogTag

        DefaultImageCache.shared.image(url: url) { [weak self] result in
            DispatchQueue.main.async {
                guard self?.tag == ogTag else {
                    return
                }
                switch result {
                case let .success(image):
                    self?.image = image
                    self?.removeActivityIndicator()
                case let .failure(err):
                    print(err)
                    self?.removeActivityIndicator()
                }
            }
        }
    }

    func cancelImageLoad() {
        DefaultImageCache.shared.cancel(nil, urlHash: tag)
    }

    func addActivityIndicator(_ style: UIActivityIndicatorView.Style = .white) {
        guard activityView() == nil else {
            return
        }
        image = UIImage(named: "TransparentPixel")
        let activityView = UIActivityIndicatorView(style: style)
        activityView.tag = Constant.activityViewTag
        addSubview(activityView)
        activityView.startAnimating()
        activityView.constrain([
             activityView.centerXAnchor.constraint(equalTo: centerXAnchor),
             activityView.centerYAnchor.constraint(equalTo: centerYAnchor)
        ])
    }
    
    func removeActivityIndicator() {
        activityView()?.removeFromSuperview()
    }

    private func activityView() -> UIActivityIndicatorView? {
        let view = subviews.first(where: { $0.tag == Constant.activityViewTag })
        return view as? UIActivityIndicatorView
    }

    private enum Constant {
        static let activityViewTag = 4398553
    }
}
