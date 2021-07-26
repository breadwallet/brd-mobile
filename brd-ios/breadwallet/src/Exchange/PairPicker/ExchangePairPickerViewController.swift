// 
//  PairPickerViewController.swift
//  breadwallet
//
//  Created by blockexplorer <michael.inger@brd.com> on 2/26/21.
//  Copyright (c) 2021 Breadwinner AG
//
//  SPDX-License-Identifier: BUSL-1.1
//

import UIKit

class ExchangePairPickerViewController: PickerViewController {

    private lazy var segmentedControl = SegmentedControl()

    private var items = [Any]()
    private var pickerViewModel: ExchangePairPickerViewModel?

    convenience init(viewModel: ExchangePairPickerViewModel) {
        self.init(viewModel: viewModel.pickerViewModel)
        pickerViewModel = viewModel
        update(with: viewModel)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        update(with: pickerViewModel)
    }

    func update(with viewModel: ExchangePairPickerViewModel?) {
        guard let viewModel = viewModel else {
            return
        }
        super.update(with: viewModel.pickerViewModel)
        segmentedControl.setSelected(viewModel.isSelectingFrom ? 0 : 1)
        segmentedControl.setItems([
            .init(title: S.Exchange.from, image: viewModel.formIcon),
            .init(title: S.Exchange.to, image: viewModel.toIcon)
        ])
    }
}

private extension ExchangePairPickerViewController {

    func setupUI() {
        segmentedControl.delegate = self
        segmentedControl.setItems([
            .init(title: S.Exchange.from),
            .init(title: S.Exchange.to)
        ])
        let segmentContainer = UIView()
        segmentedControl.translatesAutoresizingMaskIntoConstraints = false
        segmentContainer.addSubview(segmentedControl)
        segmentedControl.constrain([
            segmentedControl.leadingAnchor.constraint(greaterThanOrEqualTo: segmentContainer.leadingAnchor),
            segmentedControl.trailingAnchor.constraint(lessThanOrEqualTo: segmentContainer.trailingAnchor),
            segmentedControl.topAnchor.constraint(greaterThanOrEqualTo: segmentContainer.topAnchor),
            segmentedControl.bottomAnchor.constraint(lessThanOrEqualTo: segmentContainer.bottomAnchor),
            segmentedControl.centerXAnchor.constraint(equalTo: segmentContainer.centerXAnchor),
            segmentedControl.centerYAnchor.constraint(equalTo: segmentContainer.centerYAnchor),
            segmentedControl.widthAnchor.constraint(greaterThanOrEqualToConstant: 247),
            segmentedControl.heightAnchor.constraint(equalToConstant: 26)
        ])
        navigationItem.titleView = segmentContainer
        navigationItem.largeTitleDisplayMode = .never
        navigationController?.navigationBar.prefersLargeTitles = false
    }
}

// MARK: -

extension ExchangePairPickerViewController: SegmentedControlDelegate {

    func segmentedControl(_ segmentedControl: SegmentedControl, didSelect index: Int) {
        if index == 0 {
            pickerViewModel?.fromAction?()
        } else {
            pickerViewModel?.toAction?()
        }
    }
}
