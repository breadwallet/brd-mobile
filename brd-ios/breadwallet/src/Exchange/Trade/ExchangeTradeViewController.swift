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

enum ExchangePairComponent {
    case base
    case quote
}

final class ExchangeTradeViewController: CosmosViewController {

    private lazy var quoteInputView = CurrencyInputView()
    private lazy var baseInputView = CurrencyInputView()
    private lazy var offerView = ExchangeTradeOfferView()
    private lazy var fromLabel = UILabel(text: S.Exchange.from)
    private lazy var fromLimitLabel = UILabel(text: "")
    private lazy var toLabel = UILabel(text: S.Exchange.to)
    private lazy var spacers = [UIView(), UIView(), UIView(), UIView()]
    private lazy var swapButton = UIButton.image(UIImage(named: "ArrowDownSwap"))
    private lazy var offerLabel = UILabel(text: S.Exchange.fulfilledBy)
    private lazy var fullScreenErrorView = ExchangeFullScreenErrorView()
    private lazy var ctaButton = BRDButton(title: S.Exchange.CTA.preview)
    private lazy var limitLabel = UILabel()
    private lazy var scrollView = UIScrollView()
    private lazy var contentStack = VStackView()
    private lazy var ctaStack = VStackView()
    private lazy var consumer: TypedConsumer<ExchangeEvent>? = {
        TypedConsumer<ExchangeEvent>(optional: self.consumer())
    }()

    private let system: CoreSystem
    private var viewModel: ExchangeTradeViewModel?

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
                    view: self,
                    isTrade: true
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
        setupToHideKeyboardOnTapOnView()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.navigationBar.prefersLargeTitles = true
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - ExchangeView

extension ExchangeTradeViewController: ExchangeView {

    func update(with model: ExchangeModel?, consumer: TypedConsumer<ExchangeEvent>?) {
        guard let model = model, isViewLoaded else {
            return
        }

        guard model.mode == .trade else {
            exitAndNavigateToBuyFlow()
            return
        }

        let viewModel = ExchangeTradeViewModel(
            model: loopController?.model as? ExchangeModel ?? model,
            assetCollection: system.assetCollection,
            consumer: consumer,
            responder: currentResponder()
        )

        self.viewModel = viewModel
        fullScreenErrorView.update(with: viewModel)
        setSellFullScreenErrorHidden(fullScreenErrorView.isHidden)

        if viewModel.fullScreenErrorStyle == .emptyWallets {
            return
        }

        quoteInputView.update(with: viewModel.quoteViewModel)
        baseInputView.update(with: viewModel.baseViewModel)
        fromLimitLabel.text = viewModel.fromInfo ?? nil
        fromLimitLabel.textColor = viewModel.fromInfoColor
        ctaButton.update(with: viewModel.ctaState)
        limitLabel.text = viewModel.limit
        offerView.update(with: viewModel.offerViewModel)
        offerLabel.isHidden = offerView.isHidden
        swapButton.tap = viewModel.swapAction
        navigationItem.rightBarButtonItem?.tap = viewModel.closeAction
        ctaButton.tap = viewModel.nextAction
        ctaButton.isEnabled = true
    }

    func errorSignalAction() {
        [quoteInputView, baseInputView]
            .filter { $0.isEditing() }
            .forEach { $0.shakeAnimate() }
    }

    func popToRoot() {
        navigationController?.popToRootViewController(animated: true)
        if let visible = navigationController?.visibleViewController,
           visible != self,
           visible.isBeingDismissed == false {
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

private extension ExchangeTradeViewController {

    func addSubviews() {
        (contentViews() + [scrollView, contentStack, ctaStack, fullScreenErrorView])
            .forEach { $0.translatesAutoresizingMaskIntoConstraints = false }

        let contentStackViews = [
            HStackView([spacers[0], fromLabel, UIView(), fromLimitLabel, spacers[1]]),
            quoteInputView,
            HStackView([spacers[2], toLabel, swapButton]),
            baseInputView,
            HStackView([spacers[3], offerLabel]),
            offerView,
            fullScreenErrorView
        ]

        contentStack.addArrangedSubviews(contentStackViews)
        ctaStack.addArrangedSubviews([ctaButton, limitLabel])
        scrollView.addSubview(contentStack)
        view.addSubview(scrollView)
        view.addSubview(ctaStack)
        addNavigationBarButtons()
    }

    func setupConstraints() {
        let padding = C.padding[2] + Padding.half
        let spacing = C.padding[1] + Padding.half

        view.addConstraints([
            scrollView.topAnchor.constraint(equalTo: view.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: padding),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -padding),
            scrollView.bottomAnchor.constraint(equalTo: ctaStack.topAnchor, constant: padding),
            contentStack.topAnchor.constraint(equalTo: scrollView.topAnchor, constant: spacing),
            contentStack.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor),
            contentStack.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor),
            contentStack.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor),
            contentStack.widthAnchor.constraint(equalTo: scrollView.widthAnchor),
            ctaStack.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            ctaStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: padding),
            ctaStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -padding),
            swapButton.widthAnchor.constraint(equalToConstant: Constant.swapButtonWidth),
            swapButton.heightAnchor.constraint(greaterThanOrEqualToConstant: Constant.swapButtonHeight)
        ])

        spacers.forEach {
            view.addConstraint($0.widthAnchor.constraint(equalToConstant: Constant.fromSpacer))
        }

        ctaStack.setCustomSpacing( C.padding[1] + Padding.half, after: ctaButton)
    }

    func setInitialData() {
        title = S.Exchange.trade
        view.backgroundColor = Theme.secondaryBackground

        navigationItem.largeTitleDisplayMode = .always
        navigationController?.setDarkStyle(Theme.quaternaryBackground, hideShadow: true)

        scrollView.bounces = true
        scrollView.alwaysBounceVertical = true
        contentStack.spacing = C.padding[1] + Padding.half

        [fromLabel, toLabel, offerLabel].forEach { $0.font = Theme.body1Accent }
        [fromLimitLabel, limitLabel].forEach { $0.font = Theme.caption }
        fromLimitLabel.textColor = UIColor.failedRed
        limitLabel.textColor = Theme.tertiaryText
        swapButton.setTitleColor(UIColor.blue, for: .normal)
        offerView.backgroundColor = Theme.quaternaryBackground
        ctaButton.cornerRadius = Padding.half
        fullScreenErrorView.isHidden = true
        fullScreenErrorView.ctaAction = { [weak self] in
            self?.consumer?.accept(.OnContinueClicked())
            self?.ctaButton.isEnabled = false
        }

        update(with: loopController?.model as? ExchangeModel, consumer: nil)
    }

    func addNavigationBarButtons() {
        navigationItem.rightBarButtonItem = .close()
    }

    func setupActions() {
        navigationItem.rightBarButtonItem?.tap = { [weak self] in
            self?.consumer?.accept(.OnCloseClicked(confirmed: true))
        }
    }

    func contentViews() -> [UIView] {
        return [
            baseInputView, quoteInputView, offerView, fromLabel, fromLimitLabel,
            toLabel, swapButton, offerLabel, ctaButton, limitLabel
        ]
    }

    func setupToHideKeyboardOnTapOnView() {
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(
            target: self,
            action: #selector(dismissKeyboard)
        )
        tap.cancelsTouchesInView = false
        view.addGestureRecognizer(tap)
        scrollView.keyboardDismissMode = .onDrag
    }

    @objc func dismissKeyboard() {
        view.endEditing(true)
    }

    func ctaViews() -> [UIView] {
        [ctaButton, limitLabel]
    }

    func currentResponder() -> ExchangeTradeViewModel.Responder {
        if quoteInputView.isEditing() {
            return .quote
        }

        if baseInputView.isEditing() {
            return .base
        }

        return .none
    }

    func setSellFullScreenErrorHidden(_ hidden: Bool) {
        contentViews().forEach { $0.isHidden = !hidden }
        fullScreenErrorView.isHidden = hidden
    }

    private func exitAndNavigateToBuyFlow() {
        dismiss {
            self.disconnect()
            let action = RootModalActions.Present(modal: .buy(currency: nil))
            Store.perform(action: action)
        }
    }
}

// MARK: - Constant

private extension ExchangeTradeViewController {

    typealias Logger = SimpleLogger<ExchangeModel, ExchangeEvent, ExchangeEffect>

    enum Constant {
        static let swapButtonWidth: CGFloat = 100
        static let swapButtonHeight: CGFloat = 20
        static let fromSpacer: CGFloat = C.padding[1] + Padding.half
    }
}
