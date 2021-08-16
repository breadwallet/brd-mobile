//
//  ExchangeViewController.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import Cosmos
import UIKit
import WalletKit

final class ExchangeBuySellViewController: CosmosViewController {

    private lazy var quoteTextField = UITextField()
    private lazy var baseLabel = UILabel()
    private lazy var baseView = CellLayoutButton(accessoryStyle: .large)
    private lazy var offerView = CellLayoutButton(accessoryStyle: .large)
    private lazy var numberKeyboard = NumberKeyboardView()
    private lazy var ctaButton = BRDButton(title: S.Exchange.CTA.next)
    private lazy var limitLabel = UILabel()
    private lazy var container = VStackView()
    private lazy var consumer: TypedConsumer<ExchangeEvent>? = {
        TypedConsumer<ExchangeEvent>(optional: self.consumer())
    }()

    private let system: CoreSystem

    init(system: CoreSystem, keyStore: KeyStore, mode: ExchangeModel.Mode) {
        self.system = system
        super.init(nibName: nil, bundle: nil)

        let factory = Mobius().loop(
            update: ExchangeUpdate(),
            effectHandler: CompositeEffectHandlerCompanion.from([
                ExchangeConnectable(system: system),
                NativeExchangeConnectable(
                    system: system,
                    keyStore: keyStore,
                    view: self
                )
            ])
        )
        .doInit(init: ExchangeInit())
        .logger(logger: Logger(tag: "Exchange"))
        .eventSource(eventSource: self)

        loopController = mobiusLoopController(
            loopFactory: factory,
            defaultModel: ExchangeModel.create(mode: mode, test: E.isTestnet),
            view: ExchangeViewConnectable(view: self, system: system, keyStore: keyStore)
        )
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        addSubviews()
        setupConstraints()
        setInitialData()
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - ExchangeView

extension ExchangeBuySellViewController: ExchangeView {

    func update(with model: ExchangeModel?, consumer: TypedConsumer<ExchangeEvent>?) {
        guard let model = model, isViewLoaded else {
            return
        }

        let viewModel = ExchangeBuySellViewModel(
            model: model,
            assetCollection: system.assetCollection,
            consumer: consumer
        )

        title = viewModel.title
        quoteTextField.text = viewModel.quoteText
        quoteTextField.attributedPlaceholder = viewModel.quotePlaceholder
        baseLabel.text = viewModel.baseText
        baseLabel.textColor = viewModel.baseColor
        baseView.update(with: viewModel.baseView)
        offerView.update(with: viewModel.offer)
        ctaButton.update(with: viewModel.ctaState)
        limitLabel.text = viewModel.limit
        navigationItem.leftBarButtonItem?.tap = viewModel.settingsAction
        navigationItem.rightBarButtonItem?.tap = viewModel.closeAction
        baseView.tap = viewModel.baseAction
        offerView.tap = viewModel.offerAction
        numberKeyboard.keyTapAction = viewModel.keyAction
        ctaButton.tap = viewModel.ctaAction
        offerView.alpha = viewModel.offerEnabled ? 1.0 : 0.6
        offerView.cellLayoutView.subtitleLabel.textColor = viewModel.baseColor
        let offerLoading = viewModel.offerIsLoading
        offerLoading ? offerView.showIconSpinner() : offerView.hideIconSpinner()
        viewModel.isLoading ? baseView.showIconSpinner() : baseView.hideIconSpinner()
        contentViews().forEach { $0.alpha = viewModel.isLoading ? 0.6 : 1 }
        baseView.alpha = 1.0
    }

    func errorSignalAction() {
        let animation = ShakeNoAnimation.animation()
        quoteTextField.layer.add(animation, forKey: "position.x")
    }

    func popToRoot() {
        navigationController?.popToRootViewController(animated: true)
        if let visible = navigationController?.visibleViewController,
           visible as? UIAlertController == nil,
           visible != self {
            dismiss(animated: true)
        }
    }

    func closeAction() {
        dismiss { self.disconnect() }
    }

    func exitFlow() {
        navigationController?.popToRootViewController(animated: true)
        let visible = navigationController?.visibleViewController

        if visible == self {
            closeAction()
        } else {
            dismiss(animated: true) { [weak self] in
                self?.closeAction()
            }
        }
    }
}

// MARK: - View setup

private extension ExchangeBuySellViewController {

    func addSubviews() {
        contentViews().forEach {
            $0.translatesAutoresizingMaskIntoConstraints = false
        }

        container.addArrangedSubviews(contentViews())
        view.addSubview(container)
        addNavigationBarButtons()
    }

    func setupConstraints() {
        container.constrain([
            container.topAnchor.constraint(equalTo: view.topAnchor, constant: topPadding()),
            container.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: C.padding[2]),
            container.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -C.padding[2]),
            container.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor)
        ])

        container.spacing = spacing()
        container.setCustomSpacing(Padding.half, after: quoteTextField)
        container.setCustomSpacing(C.padding[2], after: baseView)
        container.setCustomSpacing( C.padding[1] + Padding.half, after: ctaButton)
        ctaButton.setContentHuggingPriority(.required, for: .vertical)
        limitLabel.setContentHuggingPriority(.required, for: .vertical)
        numberKeyboard.setContentHuggingPriority(.init(0), for: .vertical)
        numberKeyboard.setContentCompressionResistancePriority(.init(0), for: .vertical)

        container.arrangedSubviews.enumerated().forEach {
            $0.1.setContentHuggingPriority(.defaultHigh, for: .vertical)
        }

        [quoteTextField, baseLabel, ctaButton].forEach {
            $0.setContentCompressionResistancePriority(.required, for: .vertical)
        }
    }
    
    func setInitialData() {
        view.backgroundColor = Theme.quaternaryBackground
        navigationController?.setDarkStyle(view.backgroundColor, hideShadow: true)
        navigationController?.view.backgroundColor = view.backgroundColor
        navigationController?.navigationBar.prefersLargeTitles = false
        navigationItem.largeTitleDisplayMode = .automatic

        quoteTextField.font = UIFont.customBold(size: 60)
        quoteTextField.textColor = Theme.primaryText
        quoteTextField.textAlignment = .center
        quoteTextField.isUserInteractionEnabled = false
        quoteTextField.adjustsFontSizeToFitWidth = true

        (baseView.details, offerView.details) = ("ᐳ", "ᐳ")
        (baseLabel.text, limitLabel.text) = ("", "")

        [baseView, offerView].forEach {
            $0.backgroundColor = Theme.primaryBackground
            $0.titleStyle = .alwaysProminentTitle
            $0.iconStyle = .circle(size: Constant.iconSize)
        }

        [baseLabel, limitLabel].forEach {
            $0.textAlignment = .center
            $0.textColor = Theme.tertiaryText
            $0.font = Theme.caption
        }

        baseLabel.font = Theme.body2
        update(with: loopController?.model as? ExchangeModel, consumer: nil)
    }
    
    func addNavigationBarButtons() {
        navigationItem.rightBarButtonItem = .close()

        guard E.isDebug else {
            return
        }

        if #available(iOS 13, *) {
            let image = UIImage(systemName: "flag") ?? UIImage()
            navigationItem.leftBarButtonItem = UIBarButtonItem(image)
        } else {
            navigationItem.leftBarButtonItem = UIBarButtonItem("Settings")
        }
    }

    func contentViews() -> [UIView] {
        [quoteTextField, baseLabel, baseView, offerView, numberKeyboard,
         ctaButton, limitLabel]
    }
    
    func topPadding() -> CGFloat {
        if E.isIPhone5HeightOrLesser {
            return C.padding[1]
        }

        if E.isIPhone6OrSmaller {
            return C.padding[2]
        }

        return C.padding[4]
    }
    
    func spacing() -> CGFloat {
        if E.isIPhone5HeightOrLesser {
            return C.padding[1] + Padding.half
        }

        if E.isIPhone6OrSmaller {
            return C.padding[3] + Padding.half
        }

        return C.padding[5] + Padding.half
    }
}

// MARK: - Constant

private extension ExchangeBuySellViewController {

    typealias Logger = SimpleLogger<ExchangeModel, ExchangeEvent, ExchangeEffect>

    enum Constant {
        static let iconSize: CGFloat = 33
    }
}
