//
// Created by blockexplorer on 06/07/2021.
// Copyright (c) 2021 Breadwinner AG. All rights reserved.
//

import UIKit

class SupportHeaderView: UITableViewHeaderFooterView {

    let label = UILabel(font: Theme.h1Title, color: Theme.primaryText)

    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        setupUI()
    }

    func update(with viewModel: String?) {
        label.text = viewModel ?? ""
    }

    private func setupUI() {
        label.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(label)
        let inset = C.padding[3] + Padding.half
        label.constrain(toSuperviewEdges: UIEdgeInsets(left: inset, right: -inset))
        label.minimumScaleFactor = 0.5
        label.adjustsFontSizeToFitWidth = true
        backgroundColor = Theme.primaryBackground
        contentView.backgroundColor = Theme.primaryBackground
    }

    required init?(coder: NSCoder) {
        fatalError("Not supported")
    }
}
