// 
//  SupportArticleViewController.swift
//  breadwallet
//
//  Created by blockexplorer on 06/07/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

class SupportArticleViewController: UIViewController {

    private lazy var titleLabel = UILabel(font: Theme.h1Title, color: Theme.primaryText)
    private lazy var separator = UIView()
    private lazy var textView = UITextView()

    private var viewModel: SupportViewModel?

    func update(with viewModel: SupportViewModel) {
        self.viewModel = viewModel

        guard isViewLoaded else {
            return
        }

        titleLabel.text = viewModel.selectedArticle?.title
        textView.attributedText = viewModel.selectedArticle?.body
            .replacingOccurrences(of: " ", with: "")
            .htmlAttributedString(font: Theme.body1, color: Theme.primaryText)
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        initialSetup()

        if let viewModel = self.viewModel {
            update(with: viewModel)
        }
    }

    private func initialSetup() {
        let spacing = C.padding[3] + Padding.half
        let insets = UIEdgeInsets(forConstrains: spacing, vVal: spacing)
        let stack = VStackView([titleLabel, separator, textView])

        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.spacing = spacing
        view.addSubview(stack)
        stack.constrain(toSuperviewEdges: insets)
        separator.constrain([separator.heightAnchor.constraint(equalToConstant: 1)])

        view.backgroundColor = Theme.primaryBackground
        titleLabel.font = Theme.h1Title
        titleLabel.numberOfLines = 0
        titleLabel.textColor = Theme.primaryText
        textView.backgroundColor = view.backgroundColor
        textView.alwaysBounceVertical = true
        separator.backgroundColor = Theme.tertiaryText
        title = "Support"

        navigationItem.leftBarButtonItem = UIBarButtonItem(
            UIImage(named: "BackArrowWhite"),
            onTap: { [weak self] in self?.viewModel?.backAction?() }
        )

        navigationItem.rightBarButtonItem = .init(closeWithAction: { [weak self] in
            self?.viewModel?.closeAction?()
        })
    }
}
