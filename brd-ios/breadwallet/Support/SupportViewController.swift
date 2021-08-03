// 
//  SupportViewController.swift
//  breadwallet
//
//  Created by blockexplorer on 06/07/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit
import Cosmos

class SupportViewController: CosmosTableViewController {

    private var viewModel = SupportViewModel.empty()
    private var searchTerm: String = ""

    private let searchController = UISearchController(
        searchResultsController: nil
    )

    init(
        slug: String? = nil,
        currencyCode: String? = nil,
        style: UITableView.Style = .grouped
    ) {
        super.init(nibName: nil, bundle: nil)

        let factory = Mobius().loop(
            update: SupportUpdate(),
            effectHandler: CompositeEffectHandlerCompanion.from([
                SupportConnectable(),
                NativeSupportConnectable(view: self)
            ])
        )
        .doInit(init: SupportInit())
        .logger(logger: Logger(tag: "Support"))
        .eventSource(eventSource: self)

        loopController = mobiusLoopController(
            loopFactory: factory,
            defaultModel: SupportModel.Companion().create(
                slug: slug,
                currencyCode: currencyCode
            ),
            view: SupportViewConnectable(view: self)
        )
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        initialSetup()
        update(with: viewModel)
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return searchTerm.isEmpty ? 2 : 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        guard searchTerm.isEmpty else {
            return articles().count
        }
        
        guard section != 0 else {
            return viewModel.faqArticles.count
        }

        return viewModel.sections.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(
            withIdentifier: Constant.cellReuseId,
            for: indexPath
        )

        (cell as? SupportCell)?.update(with: titleForRow(at: indexPath))

        return cell
    }

    override func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        guard searchTerm.isEmpty else {
            return nil
        }

        let header = tableView.dequeueReusableHeaderFooterView(
            withIdentifier: Constant.sectionReuseId
        )

        (header as? SupportHeaderView)?.update(with: titleForSection(at: section))

        return header
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard searchTerm.isEmpty else {
            viewModel.selectArticleAction?(articles()[indexPath.row].id)
            return
        }

        guard indexPath.section != 0 else {
            let article = viewModel.faqArticles[indexPath.row]
            viewModel.selectArticleAction?(article.id)
            return
        }

        viewModel.selectSectionAction?(viewModel.sections[indexPath.row].id)
    }

    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return Constant.rowHeight
    }

    override func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return searchTerm.isEmpty ? Constant.headerHeight : 0
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - SupportView

extension SupportViewController: SupportView {

    func update(with viewModel: SupportViewModel) {
        self.viewModel = viewModel

        guard isViewLoaded else {
            return
        }

        tableView.reloadData()
    }

    func presentIndex(with viewModel: SupportViewModel) {
        navigationController?.popToRootViewController(animated: true)
    }

    func presentSelectedSection(with viewModel: SupportViewModel) {
        update(with: viewModel)

        let visibleVC = navigationController?.visibleViewController

        if let vc = visibleVC as? SupportSectionViewController {
            vc.update(with: viewModel)
            return
        }

        let firstSectionVc = navigationController?.viewControllers
            .map { $0 as? SupportSectionViewController }
            .compactMap { $0 }
            .first

        if let firstSectionVc = firstSectionVc {
            navigationController?.popToViewController(firstSectionVc, animated: true)
            firstSectionVc.update(with: viewModel)
            return
        }

        let sectionVc = SupportSectionViewController(style: .grouped)
        sectionVc.update(with: viewModel)
        navigationController?.setViewControllers([self, sectionVc], animated: true)
    }

    func presentSelectedArticle(with viewModel: SupportViewModel) {
        update(with: viewModel)

        let visibleVC = navigationController?.visibleViewController

        if let vc = visibleVC as? SupportArticleViewController {
            vc.update(with: viewModel)
            return
        }

        let articleVc = SupportArticleViewController()
        articleVc.update(with: viewModel)
        navigationController?.pushViewController(articleVc, animated: true)
    }

    func backAction() {
        navigationController?.popViewController(animated: true)
    }

    func closeAction() {
        disconnect()
        dismiss()
    }
}

// MARK: - UISearchResultsUpdating

extension SupportViewController: UISearchResultsUpdating {

    func updateSearchResults(for searchController: UISearchController) {
        searchTerm = searchController.searchBar.text?.lowercased() ?? ""
        viewModel.searchAction?(searchTerm)
        tableView.reloadData()
    }

    func articles() -> [SupportViewModel.Article] {
        return searchTerm.isEmpty ? viewModel.articles : viewModel.searchResults
    }
}

// MARK: - Utilities

private extension SupportViewController {

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

        searchController.searchResultsUpdater = self
        searchController.obscuresBackgroundDuringPresentation = false
        searchController.hidesNavigationBarDuringPresentation = false
        navigationItem.hidesSearchBarWhenScrolling = false
        searchController.searchBar.placeholder = "Ask a question"
        navigationItem.searchController = searchController
        navigationItem.rightBarButtonItem = .init(closeWithAction: { [weak self] in
            self?.viewModel.closeAction?()
        })
    }

    func titleForRow(at indexPath: IndexPath) -> String? {
        guard searchTerm.isEmpty else {
            return articles()[indexPath.row].title
        }

        guard indexPath.section != 0 else {
            return viewModel.faqArticles[safe: indexPath.row]?.title
        }

        return viewModel.sections[safe: indexPath.row]?.title
    }

    func titleForSection(at index: Int) -> String {
        if index == 0 {
            return "Frequently asked questions"
        }

        return "Browse topics"
    }
}

// MARK: - Constants

private extension SupportViewController {

    typealias Logger = SimpleLogger<SupportModel, SupportEvent, SupportEffect>

    enum Constant {
        static let cellReuseId = "SupportViewController.Cell"
        static let sectionReuseId = "SupportViewController.Section"
        static let rowHeight = CGFloat(78)
        static let headerHeight = CGFloat(63)
    }
}
