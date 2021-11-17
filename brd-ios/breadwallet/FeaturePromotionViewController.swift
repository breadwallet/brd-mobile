//
//  FeaturePromotionViewController.swift
//  breadwallet
//
//  Created by blockexplorer on 2021-08-31.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class FeaturePromotionViewController: UIViewController {

    private lazy var imageView = UIImageView()
    private lazy var imageViewContainer = VStackView([imageView])
    private lazy var titleLabel = UILabel(font: Theme.h1TitleAccent, color: Theme.primaryText)
    private lazy var bodyLabel = UILabel(font: Theme.body1, color: Theme.secondaryText)
    private lazy var button = BRDButton(title: "", type: .secondary)
    private lazy var stack = VStackView(
        arrangedSubviews: [imageViewContainer, titleLabel, bodyLabel, button]
    )
    
    private var viewModel: ViewModel

    init(with viewModel: ViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .overCurrentContext
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        stack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stack)
        let hPadding: CGFloat = C.padding[4] + 1
        let vPadding: CGFloat = C.padding[3]
        [
            stack.topAnchor.constraint(
                equalTo: view.safeAreaLayoutGuide.topAnchor,
                constant: vPadding
            ),
            stack.bottomAnchor.constraint(
                equalTo: view.safeAreaLayoutGuide.bottomAnchor,
                constant: -vPadding
            ),
            stack.leadingAnchor.constraint(
                equalTo: view.leadingAnchor,
                constant: hPadding
            ),
            stack.trailingAnchor.constraint(
                equalTo: view.trailingAnchor,
                constant: -hPadding
            )
        ].forEach { $0.isActive = true }
        
        
        update(with: viewModel)
        setInitialData()
    }
    
    func update(with viewModel: ViewModel) {
        imageView.image = UIImage(named: viewModel.imageName)
        titleLabel.text = viewModel.title
        bodyLabel.text = viewModel.body
        button.title = viewModel.button
        button.tap = viewModel.action
        button.label.textColor = viewModel.backgroundColor
        view.backgroundColor = viewModel.backgroundColor
    }
    
    private func setInitialData() {
        stack.alignment = .fill
        stack.distribution = .equalSpacing
        imageView.contentMode = .scaleAspectFit
        imageViewContainer.alignment = .center
        imageViewContainer.distribution = .equalCentering
        [titleLabel, bodyLabel].forEach {
            $0.numberOfLines = 0
            $0.minimumScaleFactor = 0.7
            $0.adjustsFontSizeToFitWidth = true
        }
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        .lightContent
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - ViewModel

extension FeaturePromotionViewController {
        
    struct ViewModel {
        let backgroundColor: UIColor
        let imageName: String
        let title: String
        let body: String
        let button: String
        let action: (() -> Void)?
    }
}

// MARK: - Hydra buy promotion

extension FeaturePromotionViewController.ViewModel {
    
    typealias FeaturePromotionViewModel = FeaturePromotionViewController.ViewModel
    typealias Action = (() -> Void)
    
    static func hydraBuy(_ action: Action? = nil) -> FeaturePromotionViewModel {
        return .init(
            backgroundColor: UIColor.fromHex("416DE9"),
            imageName: "promoBuy",
            title: S.Exchange.FeaturePromotion.buyTitle,
            body: S.Exchange.FeaturePromotion.buyBody,
            button: S.Exchange.FeaturePromotion.CTA,
            action: action
        )
    }
}

// MARK: - Hydra trade promotion

extension FeaturePromotionViewController.ViewModel {
        
    static func hydraTrade(_ action: Action? = nil) -> FeaturePromotionViewModel {
        return .init(
            backgroundColor: UIColor.fromHex("#FE5094"),
            imageName: "promoTrade",
            title: S.Exchange.FeaturePromotion.tradeTitle,
            body: S.Exchange.FeaturePromotion.tradeBody,
            button: S.Exchange.FeaturePromotion.CTA,
            action: action
        )
    }
}
