//
// Created by blockexplorer on 06/07/2021.
// Copyright (c) 2021 Breadwinner AG. All rights reserved.
//

import UIKit

class SupportCell: UITableViewCell {

    let label = UILabel(font: Theme.body1, color: Theme.quaternaryText)

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }

    func update(with viewModel: String?) {
        label.text = viewModel ?? ""
    }

    required init?(coder: NSCoder) {
        fatalError("Not supported")
    }

    private func setupUI() {
        label.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(label)
        let inset = C.padding[3] + Padding.half
        label.constrain(toSuperviewEdges: UIEdgeInsets(left: inset, right: -inset))
        label.numberOfLines = 0
        backgroundColor = Theme.primaryBackground
        selectionStyle = .none
    }
}
