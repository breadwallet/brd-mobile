// 
//  ExchangeTradePreviewViewController.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeTradePreviewViewController: UIViewController {

    private lazy var overScrollView = UIView()
    private lazy var tableView = UITableView(frame: .zero, style: .grouped)
    private lazy var ctaButton = BRDButton(title: S.Exchange.Preview.cta)

    private var viewModel: ExchangeTradePreviewViewModel?
    private var expanded = false
    private var closeOnDeinit = true

    init(viewModel: ExchangeTradePreviewViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.register(
            ExchangeTradePreviewCell.self,
            forCellReuseIdentifier: Constant.cellReuseId
        )

        tableView.register(
            ExchangeTradePreviewHeader.self,
            forHeaderFooterViewReuseIdentifier: Constant.headerReuseId
        )

        tableView.register(
            ExchangeTradePreviewFooter.self,
            forHeaderFooterViewReuseIdentifier: Constant.footerReuseId
        )

        setupUI()
        update(with: viewModel)
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        scrollViewDidScroll(tableView)
    }

    func update(with viewModel: ExchangeTradePreviewViewModel?) {
        self.viewModel = viewModel
        ctaButton.update(with: viewModel?.ctaState ?? .next)
        navigationItem.leftBarButtonItem?.isEnabled = viewModel?.ctaState.isEnabled() ?? false
        tableView.reloadData()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - UITableViewDataSource

extension ExchangeTradePreviewViewController: UITableViewDataSource {

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items().count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(
            withIdentifier: Constant.cellReuseId,
            for: indexPath
        )

        let previewCell = cell as? ExchangeTradePreviewCell
        previewCell?.update(with: items()[indexPath.row], expanded: expanded)
        previewCell?.backgroundColor = Theme.primaryBackground
        previewCell?.topAlightTitle = isInactiveRewards(indexPath)
        return cell
    }

    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let reusableView = tableView.dequeueReusableHeaderFooterView(
            withIdentifier: Constant.headerReuseId
        )

        let header = reusableView as? ExchangeTradePreviewHeader
        header?.update(with: viewModel?.header)
        return reusableView
    }

    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let reusableView = tableView.dequeueReusableHeaderFooterView(
            withIdentifier: Constant.footerReuseId
        )

        let footer = reusableView as? ExchangeTradePreviewFooter
        footer?.label.text = viewModel?.footer ?? ""
        footer?.contentView.backgroundColor = view?.backgroundColor
        return footer
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return isInactiveRewards(indexPath)
            ? Constant.inactiveRewardsRowHeight
            : Constant.rowHeight
    }

    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return Constant.headerHeight
    }
}

// MARK: - UITableViewDelegate

extension ExchangeTradePreviewViewController: UITableViewDelegate {

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard let rewardIdx = rewardsItemIndex(), rewardIdx != indexPath.row else {
            viewModel?.rewardsDetailAction?()
            return
        }

        guard let idx = feeTotalItemIndex(), idx == indexPath.row else {
            return
        }

        expanded.toggle()
        let items = viewModel?.infoItems ?? []
        let idxPaths = items.enumerated()
            .filter { $0.1.isFee }
            .map {  IndexPath(row: $0.0, section: 0)}
        tableView.beginUpdates()
        tableView.reloadRows(at: [indexPath], with: .automatic)
        if expanded {
            tableView.insertRows(at: idxPaths, with: .bottom)
        } else {
            tableView.deleteRows(at: idxPaths, with: .bottom)
        }
        tableView.endUpdates()
    }

    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        guard let header = tableView.headerView(forSection: 0) else {
            return
        }

        let size = CGSize(
            width: view.bounds.width,
            height: header.convert(header.bounds, to: overScrollView.superview).maxY
        )

        overScrollView.frame = CGRect(origin: .zero, size: size)
    }
}

// MARK: - UI setup

private extension ExchangeTradePreviewViewController {

    func setupUI() {
        title = S.Exchange.Preview.title
        navigationItem.largeTitleDisplayMode = .never

        [overScrollView, tableView, ctaButton].forEach {
            $0.translatesAutoresizingMaskIntoConstraints = false
            view.addSubview($0)
        }

        view.backgroundColor = Theme.primaryBackground
        overScrollView.backgroundColor = Theme.quaternaryBackground

        let padding = C.padding[2] + Padding.half
        let tablePadding = C.padding[4] + Padding.half
        tableView.dataSource = self
        tableView.delegate = self
        tableView.separatorInset = UIEdgeInsets.zero
        tableView.backgroundColor = .clear
        tableView.setContentHuggingPriority(.defaultLow, for: .vertical)
        tableView.separatorColor = Theme.primaryText.withAlphaComponent(0.1)
        tableView.scrollIndicatorInsets = UIEdgeInsets(aTop: Constant.headerHeight, right: -C.padding[4])
        view.addConstraints([
            tableView.topAnchor.constraint(equalTo: view.topAnchor),
            tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: tablePadding),
            tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -tablePadding),
            tableView.bottomAnchor.constraint(equalTo: ctaButton.topAnchor),
            ctaButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: padding),
            ctaButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -padding),
            ctaButton.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor)
        ])

        ctaButton.tap = { [weak self] in self?.ctaAction(nil) }
        navigationItem.leftBarButtonItem = UIBarButtonItem(
                image: UIImage(named: "BackArrowWhite"),
                style: .plain,
                target: self,
                action: #selector(backAction(_:))
        )
        navigationController?.interactivePopGestureRecognizer?.delegate = nil
        navigationController?.interactivePopGestureRecognizer?.isEnabled = true
    }

    func items() -> [ExchangeTradePreviewViewModel.InfoItem] {
        if expanded {
            return viewModel?.infoItems ?? []
        }
        return viewModel?.infoItems.filter { !$0.isFee } ?? []
    }

    func feeTotalItemIndex() -> Int? {
        return items().firstIndex(where: { $0.isFeeTotal })
    }

    func rewardsItemIndex() -> Int? {
        return items().firstIndex(where: { $0.isRewards })
    }

    func isInactiveRewards(_ indexPath: IndexPath) -> Bool {
        switch items()[indexPath.row] {
        case let .reward(active):
            return active == false
        default:
            return false
        }
    }

    @objc func ctaAction(_ sender: Any?) {
        viewModel?.ctaAction?()
    }

    @objc func backAction(_ sender: Any?) {
        closeOnDeinit = false
        viewModel?.closeAction?()
    }
}

// MARK: - UI setup

private extension ExchangeTradePreviewViewController {

    enum Constant {
        static let cellReuseId = "cellReuseId"
        static let headerReuseId = "headerReuseId"
        static let footerReuseId = "footerReuseId"
        static let headerHeight = CGFloat(168)
        static let rowHeight = CGFloat(56)
        static let inactiveRewardsRowHeight = CGFloat(75)
    }
}
