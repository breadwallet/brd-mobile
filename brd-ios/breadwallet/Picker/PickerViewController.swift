// 
//  PickerViewController.swift
//  breadwallet
//
//  Created by stringcode on 25/03/2021.
//  Copyright © 2021 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

class PickerViewController: UITableViewController {

    private var searchTerm: String = ""
    private var searchItems: [PickerViewModel.Item] = []
    private var closeOnDeinit: Bool = true

    private(set) var viewModel: PickerViewModel = .empty()

    private let searchController = UISearchController(
        searchResultsController: nil
    )

    convenience init(viewModel: PickerViewModel) {
        self.init(nibName: nil, bundle: nil)
        definesPresentationContext = true
        update(with: viewModel)
    }

    override func loadView() {
        super.loadView()
        view.backgroundColor = Theme.secondaryBackground
        navigationItem.rightBarButtonItem = UIBarButtonItem.close(
            self,
            action: #selector(closeAction(_:))
        )
        tableView.register(
            PickerTableViewCell.self,
            forCellReuseIdentifier: Constant.cellReuseId
        )
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupSearchViewController()
        tableView.separatorColor = Theme.primaryText.withAlphaComponent(0.1)
        tableView.separatorInset = UIEdgeInsets(
            left: C.padding[3],
            right: C.padding[3]
        )
        view.backgroundColor = viewModel.backgroundColor
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        updateSelectedCell()
    }

    func update(with viewModel: PickerViewModel) {
        self.viewModel = viewModel
        navigationItem.title = viewModel.title
        navigationItem.largeTitleDisplayMode = viewModel.titleDisplayMode
        navigationItem.hidesBackButton = viewModel.disableBackButton
        setToolbarItems(viewModel.toolBarItems, animated: true)
        tableView.contentInset = viewModel.sectionInsets

        if isViewLoaded {
            view.backgroundColor = viewModel.backgroundColor
            tableView.reloadData()
            updateSelectedCell()
        }

        if viewModel.backAction != nil {
            navigationItem.leftBarButtonItem = UIBarButtonItem(
                image: UIImage(named: "BackArrowWhite"),
                style: .plain,
                target: self,
                action: #selector(backAction(_:))
            )
        }
    }

    deinit {
        if closeOnDeinit {
            viewModel.closeAction?()
        }
    }

    // MARK: - UITableViewDataSource

    override func numberOfSections(in tableView: UITableView) -> Int {
         return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return itemCount()
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(
            withIdentifier: Constant.cellReuseId,
            for: indexPath
        )
        let pickerCell = cell as? PickerTableViewCell
        let item = itemAt(indexPath.row)
        pickerCell?.update(with: item)
        let hideIcon = item.iconImage == nil && item.iconURL == nil && item.iconText == nil
        pickerCell?.cellLayoutView.iconStyle = hideIcon ? .default : .square(size: Constant.iconSize)
        pickerCell?.cellLayoutView.titleStyle = .alwaysProminentTitle
        pickerCell?.cellLayoutView.rightTitleStyle = .alwaysProminentTitle
        pickerCell?.backgroundColor = view.backgroundColor
        pickerCell?.isSelected = viewModel.selectedIndexes.contains(indexPath.row)
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        print("=== DID SELECT", indexPath.row)
        closeOnDeinit = false
        viewModel.selectedAction?(indexForItem(at: indexPath))
    }

    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return viewModel.prefersLargerCells ? Constant.cellHeightLarger : Constant.cellHeight
    }

    // MARK: - UITableViewDelegate

    override func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        let selected = viewModel.selectedIndexes.contains(indexPath.row)
        cell.setSelected(selected, animated: true)
        // NOTE: Workaround for broken layout
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { [weak self] in
            let selected = self?.viewModel.selectedIndexes.contains(indexPath.row) ?? false
            cell.setSelected(selected, animated: true)
        }
    }

    private func updateSelectedCell(_ animated: Bool = false) {
        viewModel.selectedIndexes.forEach {
            tableView.selectRow(
                at: IndexPath(row: $0, section: 0),
                animated: animated,
                scrollPosition: .middle
            )
        }
    }
}

// MARK: - Actions

private extension PickerViewController {

    @objc func backAction(_ sender: Any?) {
        closeOnDeinit = false
        viewModel.backAction?()
    }

    @objc func closeAction(_ sender: Any?) {
        closeOnDeinit = false
        viewModel.closeAction?()
    }
}

// MARK: - Search handling

private extension PickerViewController {

    func items() -> [PickerViewModel.Item] {
        return searchTerm.isEmpty ? viewModel.items : searchItems
    }

    func itemCount() -> Int {
        return items().count
    }

    func itemAt(_ idx: Int) -> PickerViewModel.Item {
        return items()[idx]
    }

    func indexForItem(at idxPath: IndexPath) -> Int {
        if !searchTerm.isEmpty {
            let searchItem = items()[idxPath.row]
            let idx = viewModel.items.firstIndex(of: searchItem)
            return idx ?? idxPath.row
        }
        return idxPath.row
    }

    func setupSearchViewController() {
        navigationController?.navigationBar.prefersLargeTitles = true
        navigationItem.largeTitleDisplayMode = .always
        searchController.searchResultsUpdater = self
        searchController.obscuresBackgroundDuringPresentation = false
        searchController.hidesNavigationBarDuringPresentation = false
        searchController.searchBar.placeholder = "Search"
        navigationItem.searchController = searchController
        navigationItem.hidesSearchBarWhenScrolling = false
    }
}

// MARK: - UISearchResultsUpdating

extension PickerViewController: UISearchResultsUpdating {

    func updateSearchResults(for searchController: UISearchController) {
        searchTerm = searchController.searchBar.text?.lowercased() ?? ""
        searchItems = viewModel.items.filter {
            [$0.title, $0.subtitle, $0.detail]
                .map { $0?.lowercased() }
                .first(where: { $0?.fuzzyMatch(searchTerm) ?? false }) != nil
        }
        tableView.reloadSections([0], with: .automatic)
    }
}

// MARK: - Constant

private extension PickerViewController {

    enum Constant {
        static let cellReuseId = "PickerViewController.CellReuseId"
        static let cellHeight = CGFloat(64)
        static let cellHeightLarger = CGFloat(75)
        static let iconSize = CGFloat(36)
    }
}
