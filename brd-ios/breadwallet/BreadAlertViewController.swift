//
//  BreadAlertViewController.swift
//  breadwallet
//
//  Created by blockexplorer on 2021-08-31.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class BreadAlertViewController: UIViewController {
    
    weak var modalDismissDelegate: ModalDismissDelegate?

    private lazy var imageView = UIImageView()
    private lazy var imageViewContainer = VStackView([imageView])
    private lazy var titleLabel = UILabel(font: Theme.h1Title, color: Theme.primaryText)
    private lazy var bodyLabel = UILabel(font: Theme.body2, color: Theme.secondaryText)
    private lazy var buttons: [BRDButton] = []
    private lazy var stack = VStackView([imageViewContainer, titleLabel, bodyLabel])
    
    private var viewModel: ViewModel
    
    init(viewModel: ViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .custom
        transitioningDelegate = self
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        stack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stack)
        let vPad = C.padding[3] + Padding.half
        let hPad = C.padding[2]
        [
            stack.topAnchor.constraint(equalTo: view.topAnchor, constant: vPad),
            stack.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -vPad),
            stack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: hPad),
            stack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -hPad)
        ].forEach { $0.isActive = true }

        setInitialData()
        update(with: viewModel)
    }

    func update(with viewModel: ViewModel) {
        imageView.image = UIImage(named: viewModel.imageName)
        titleLabel.text = viewModel.title
        bodyLabel.text = viewModel.body
        for (idx, item) in viewModel.buttons.enumerated() {
            if buttons.count > viewModel.buttons.count {
                updateButton(buttons[idx], with: item)
                continue
            }
            let button = BRDButton(title: item.title)
            buttons.append(button)
            updateButton(button, with: item)
            stack.addArrangedSubview(button)
        }
        
        while buttons.count > viewModel.buttons.count {
            buttons.removeLast().removeFromSuperview()
        }

        buttons.forEach {
            stack.setCustomSpacing(C.padding[1] + Padding.half, after: $0)
        }
    }
    
    private func updateButton(_ button: BRDButton, with viewModel: ViewModel.Button) {
        button.title = viewModel.title
        button.alpha = viewModel.style == .default ? 1 : 0.5
        button.tap = viewModel.action
    }

    private func setInitialData() {
        stack.distribution = .fill
        stack.alignment = .fill
        stack.spacing = C.padding[3]
        imageViewContainer.alignment = .center
        titleLabel.textAlignment = .center
        bodyLabel.numberOfLines = 0
        bodyLabel.textAlignment = .center
        view.backgroundColor = Theme.quaternaryBackground
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - Presentation

extension BreadAlertViewController: ModalDismissProtocol, UIViewControllerTransitioningDelegate {
    
    @IBAction func cancelAction(_ sender: Any) {
        modalDismissDelegate?.viewControllerDismissActionPressed(self)
    }
    
    func presentationController(
        forPresented presented: UIViewController,
        presenting: UIViewController?,
        source: UIViewController
    ) -> UIPresentationController? {
        return PreferredSizePresentationController(
            presentedViewController: presented,
            presenting: presenting
        )
    }
}

// MARK: - ViewModel

extension BreadAlertViewController {

    struct ViewModel {
        let imageName: String
        let title: String
        let body: String
        let buttons: [Button]
        
        struct Button {
            let title: String
            let style: Style
            let action: (() -> Void)?
            
            enum Style{
                case `default`
                case cancel
            }
        }
    }
}

// MARK: - Model ViewModel

extension BreadAlertViewController.ViewModel {

    static func mock() -> BreadAlertViewController.ViewModel {
        .init(
            imageName: "alertErrorLarge",
            title: "ETH Balance Low",
            body: "BAT uses the Ethereum network which requires ETH to pay transaction fees.",
            buttons: [
                .init(
                    title: "Top up ETH balance",
                    style: .default,
                    action: nil
                ),
                .init(
                    title: "Close",
                    style: .cancel,
                    action: nil
                )
            ]
        )
    }
}
