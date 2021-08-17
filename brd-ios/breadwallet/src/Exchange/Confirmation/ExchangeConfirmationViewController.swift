// 
//  ExchangeOrderController.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit
import Cosmos

class ExchangeConfirmationViewController: UIViewController {

    private(set) var viewModel: ExchangeConfirmationViewModel

    private lazy var headerView = ExchangeConfirmationStatusHeaderView()
    private lazy var tableView = UITableView()
    private lazy var buttonsContainer = HStackView([receiptButton, doneButton])
    private lazy var doneButton = BRDButton(title: S.Exchange.Order.CTA.great)
    private lazy var receiptButton = BRDButton(
        title: S.Exchange.Order.CTA.receipt,
        type: .secondaryTransparent
    )

    private var didAppear = false
    private var feesExpanded = false

    init(_ viewModel: ExchangeConfirmationViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        update(with: viewModel)
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        didAppear = true
        if headerView.state == .successful {
            headerView.animateConfetty()
        }
    }

    func update(with viewModel: ExchangeConfirmationViewModel) {
        self.viewModel = viewModel
        switch viewModel.state {
        case .creating:
            headerView.setState(.creating)
            tableView.reloadData()
            buttonsContainer.isHidden = true
        case .processing:
            headerView.setState(.processing)
            tableView.reloadData()
            buttonsContainer.isHidden = true
        case let .complete(order):
            headerView.setState(.successful)
            tableView.reloadData()
            buttonsContainer.isHidden = false
            if didAppear {
                headerView.animateConfetty()
            }
        }
        doneButton.tap = viewModel.doneAction
        receiptButton.tap = { [weak self] in
            self?.dismiss {
                Store.trigger(name: .presentOrderHistory)
            }
        }
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - UI Setup

private extension ExchangeConfirmationViewController {

    func setupUI() {
        let vStack = VStackView([tableView, buttonsContainer])

        view.backgroundColor = Theme.primaryBackground
        view.addSubview(vStack)
        view.addSubview(headerView)

        let padding = C.padding[3]
        vStack.constrain([
            vStack.topAnchor.constraint(equalTo: headerView.bottomAnchor, constant: C.padding[4]),
            vStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: padding),
            vStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -padding),
            vStack.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -padding)
        ])
        vStack.spacing = C.padding[4]
        vStack.backgroundColor = Theme.primaryBackground

        headerView.constrain([
            headerView.topAnchor.constraint(equalTo: view.topAnchor),
            headerView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            headerView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            headerView.heightAnchor.constraint(equalTo: view.heightAnchor, multiplier: 0.33)
        ])

        tableView.setContentHuggingPriority(.defaultLow, for: .vertical)
        tableView.delegate = self
        tableView.dataSource = self
        tableView.backgroundColor = view.backgroundColor
        tableView.separatorInset = UIEdgeInsets.zero
        tableView.separatorColor = Theme.primaryText.withAlphaComponent(0.1)
        tableView.register(
            ExchangeConfirmationCell.self,
            forCellReuseIdentifier: Constant.reuseId
        )
        tableView.register(
            ExchangeConfirmationFooterView.self,
            forHeaderFooterViewReuseIdentifier: Constant.footerReuseId
        )

        buttonsContainer.distribution = .fillEqually
        buttonsContainer.spacing = C.padding[1]
        buttonsContainer.arrangedSubviews.forEach {
            $0.layer.cornerRadius = C.padding[1]
            $0.clipsToBounds = true
        }

        navigationController?.setNavigationBarHidden(true, animated: true)
    }
}

// MARK: - UITableViewDataSource

extension ExchangeConfirmationViewController: UITableViewDataSource {

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch viewModel.state {
        case .creating:
            return 0
        case let .processing(order):
            return 5 + (feesExpanded ? order.fees.count : 0)
        case let .complete(order):
            return 5 + (feesExpanded ? order.fees.count : 0)
        }
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(
            withIdentifier: Constant.reuseId,
            for: indexPath
        )
        let orderCell = cell as? ExchangeConfirmationCell
        let order = viewModel.order()

        switch indexPath.row {
        case 0:
            orderCell?.title = S.Exchange.Order.bought
            orderCell?.detail = order?.baseAmount ?? ""
            orderCell?.textColor = order?.baseColor ?? Theme.primaryText
        case 1:
            orderCell?.title = S.Exchange.Order.for
            orderCell?.detail = order?.quoteAmount ?? ""
            orderCell?.textColor = order?.quoteColor ?? Theme.primaryText
        case 2:
            orderCell?.title = S.Exchange.Order.method
            orderCell?.detail = order?.method ?? ""
        case 3:
            orderCell?.title = S.Exchange.Order.delivery
            orderCell?.detail = order?.delivery ?? ""
        case 4:
            orderCell?.title = S.Exchange.Order.fees
            orderCell?.detail = order?.feeTotal ?? ""
        default:
            let idx = indexPath.row - 5
            if let fees = order?.fees, fees.count > idx {
                orderCell?.title = fees[idx].title
                orderCell?.detail = fees[idx].amount
            }
        }
        cell.backgroundColor = view.backgroundColor
        return cell
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return C.padding[7]
    }

    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let reusableView = tableView.dequeueReusableHeaderFooterView(
            withIdentifier: Constant.footerReuseId
        )
        let footer = reusableView as? ExchangeConfirmationFooterView
        footer?.textLabel?.text = viewModel.footerInfo ?? ""
        footer?.contentView.backgroundColor = view?.backgroundColor
        return footer
    }

    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return C.padding[7]
    }
}

// MARK: - UITableViewDelegate

extension ExchangeConfirmationViewController: UITableViewDelegate {

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.row == 4 {
            feesExpanded = !feesExpanded
            tableView.reloadSections(IndexSet([0]), with: .none)
        }
    }
}

// MARK: - Constant

private extension ExchangeConfirmationViewController {

    enum Constant {
        static let reuseId = "reuseId"
        static let footerReuseId = "footerReuseId"
    }
}
