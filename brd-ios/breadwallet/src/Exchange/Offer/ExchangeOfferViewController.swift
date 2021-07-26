//
//  ExchangeOfferViewController.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangeOfferViewController: UITableViewController {

    private var viewModel: ExchangeOfferViewModel?
    private var closeOnDeinit: Bool = true

    convenience init(viewModel: ExchangeOfferViewModel) {
        self.init(nibName: nil, bundle: nil)
        self.viewModel = viewModel
        definesPresentationContext = true
    }

    override func loadView() {
        super.loadView()
        tableView.register(
            ExchangeOfferCell.self,
            forCellReuseIdentifier: Constant.cellReuseId
        )
        tableView.register(
            ExchangeOfferHeaderView.self,
            forHeaderFooterViewReuseIdentifier: Constant.headerReuseId
        )
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        update(with: viewModel)
    }

    func update(with viewModel: ExchangeOfferViewModel?) {
        self.viewModel = viewModel
        navigationItem.title = viewModel?.title ?? ""
        navigationItem.rightBarButtonItem?.tap = { [weak self] in
            self?.closeOnDeinit = false
            self?.viewModel?.closeAction?()
        }
        if isViewLoaded && viewModel != self.viewModel {
            tableView.reloadData()
        }
    }

    deinit {
        if closeOnDeinit {
            viewModel?.closeAction?()
        }
    }

    // MARK: - TableViewDataSource

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(
        _ tableView: UITableView,
        numberOfRowsInSection section: Int
    ) -> Int {
        return viewModel?.offers.count ?? 0
    }

    override func tableView(
        _ tableView: UITableView,
        cellForRowAt indexPath: IndexPath
    ) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(
            withIdentifier: Constant.cellReuseId,
            for: indexPath
        )

        let offerCell = cell as? ExchangeOfferCell
        offerCell?.update(viewModel?.offers[indexPath.row])
        offerCell?.minMaxButton.tap = { [weak self] in
            self?.closeOnDeinit = false
            self?.viewModel?.minMaxAction?(indexPath.row)
        }

        return cell
    }

    override func tableView(
        _ tableView: UITableView,
        viewForHeaderInSection section: Int
    ) -> UIView? {
        let view = tableView.dequeueReusableHeaderFooterView(
            withIdentifier: Constant.headerReuseId
        )

        let header = view as? ExchangeOfferHeaderView
        header?.textLabel?.text = viewModel?.sectionTitle ?? ""
        header?.contentView.backgroundColor = Theme.primaryBackground
        return header
    }

    override func tableView(
        _ tableView: UITableView,
        heightForHeaderInSection section: Int
    ) -> CGFloat {
        return C.padding[8]
    }

    override func tableView(
        _ tableView: UITableView,
        didSelectRowAt indexPath: IndexPath
    ) {
        closeOnDeinit = false
        viewModel?.selectedAction?(indexPath.row)
    }
}

// MARK: - Setup UI

private extension  ExchangeOfferViewController {

    func setupUI() {
        navigationItem.rightBarButtonItem = UIBarButtonItem.close()
        view.backgroundColor = Theme.primaryBackground
        tableView.separatorStyle = .none
    }
}

// MARK: - Constant

private extension ExchangeOfferViewController {

    enum Constant {
        static let cellReuseId = "OfferCellReuseId"
        static let headerReuseId = "OfferCellReuseId"
    }
}
