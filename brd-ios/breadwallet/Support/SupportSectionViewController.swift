// 
//  SupportSectionViewController.swift
//  breadwallet
//
//  Created by blockexplorer on 06/07/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

class SupportSectionViewController: UITableViewController {

    private var viewModel: SupportViewModel?
    private var articles: [SupportViewModel.Article] = []

    func update(with viewModel: SupportViewModel) {
        self.viewModel = viewModel
        self.articles = viewModel.articles
            .filter { $0.sectionId == (viewModel.selectedSection?.id ?? -1) }

        guard isViewLoaded else {
            return
        }

        tableView.reloadData()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        initialSetup()
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return articles.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(
            withIdentifier: Constant.cellReuseId,
            for: indexPath
        )

        (cell as? SupportCell)?.update(with: articles[indexPath.row].title)

        return cell
    }

    override func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let header = tableView.dequeueReusableHeaderFooterView(
            withIdentifier: Constant.sectionReuseId
        )

        (header as? SupportHeaderView)?.update(with: viewModel?.selectedSection?.title)

        return header
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        viewModel?.selectArticleAction?(articles[indexPath.row].id)
    }

    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return Constant.rowHeight
    }

    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return Constant.headerHeight
    }
}

private extension SupportSectionViewController {

    func initialSetup() {
        tableView.register(
            SupportCell.self,
            forCellReuseIdentifier: Constant.cellReuseId
        )

        tableView.register(
            SupportHeaderView.self,
            forHeaderFooterViewReuseIdentifier: Constant.sectionReuseId
        )

        tableView.backgroundColor = Theme.primaryBackground
        let inset = C.padding[3] + Padding.half
        tableView.separatorInset = UIEdgeInsets(left: inset, right: inset)
        tableView.separatorColor = Theme.primaryText.withAlphaComponent(0.2)
        title = "Support"

        navigationItem.rightBarButtonItem = .init(closeWithAction: { [weak self] in
            self?.viewModel?.closeAction?()
        })
    }
}

// MARK: - Constant

private extension SupportSectionViewController {

    enum Constant {
        static let cellReuseId = "SupportSectionViewController.Cell"
        static let sectionReuseId = "SupportSectionViewController.Section"
        static let rowHeight = CGFloat(78)
        static let headerHeight = CGFloat(63)
    }
}
