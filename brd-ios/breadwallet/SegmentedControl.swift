//
// Created by blockexplorer on 13/05/2021.
// Copyright (c) 2021 Breadwinner AG. All rights reserved.
//

import UIKit

protocol SegmentedControlDelegate: class {
    func segmentedControl(_ segmentedControl: SegmentedControl, didSelect index: Int)
}

class SegmentedControl: UIView {

    var textColor = Theme.primaryText {
        didSet { updateColors() }
    }

    var selectedTextColor = Theme.primaryBackground {
        didSet { updateColors() }
    }

    var highlightColor = Theme.primaryText {
        didSet { updateColors() }
    }

    weak var delegate: SegmentedControlDelegate?

    private(set) var selectedIdx = 0

    private lazy var highlightView = UIView()
    private lazy var container = HStackView()
    private lazy var items = [Item]()

    init(_ items: [Item] = []) {
        super.init(frame: .zero)
        setupUI()
        setItems(items)
        setSelected(0)
    }

    func setItems(_ items: [Item]) {
        container.arrangedSubviews.forEach { $0.removeFromSuperview() }
        let buttons = items.enumerated().map { (idx, item) in
            self.makeButton(item, idx: idx)
        }
        container.addArrangedSubviews(buttons)
        self.items = items
        updateColors()
    }

    func currentItems() -> [Item] {
        items
    }

    func setSelected(_ idx: Int, animated: Bool = false) {
        guard let selectedItem = container.arrangedSubviews[safe: idx] else {
            return
        }

        let selectedFrame = selectedItem.convert(selectedItem.bounds, to: self)
        selectedIdx = idx

        guard animated else {
            highlightView.frame = selectedFrame
            updateColors()
            return
        }

        UIView.animate(withDuration: 0.2) {
            self.highlightView.frame = selectedFrame
            self.updateColors()
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        if highlightView.frame.isEmpty {
            setSelected(selectedIdx)
        }
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - UISetup

extension SegmentedControl {

    func setupUI() {
        let padding = C.padding[1]
        container.translatesAutoresizingMaskIntoConstraints = false
        addSubview(highlightView)
        addSubview(container)
        container.distribution = .fillEqually
        container.constrain([
            container.leadingAnchor.constraint(equalTo: leadingAnchor),
            container.trailingAnchor.constraint(equalTo: trailingAnchor),
            container.topAnchor.constraint(equalTo: topAnchor),
            container.bottomAnchor.constraint(equalTo: bottomAnchor),
        ])
        container.distribution = .fillEqually
        layer.cornerRadius = C.padding[1]
        highlightView.layer.cornerRadius = C.padding[1]
        backgroundColor = Theme.secondaryBackground
    }

    func tappedItem(_ item: Item, at idx: Int) {
        let didChange = selectedIdx != idx
        setSelected(idx, animated: true)
        didChange ? delegate?.segmentedControl(self, didSelect: idx) : ()
    }

    func updateColors() {
        highlightView.backgroundColor = highlightColor
        container.arrangedSubviews.forEach {
            ($0 as? Button)?.label?.textColor = textColor
        }
        (container.arrangedSubviews[safe: selectedIdx] as? Button)?
                .label?.textColor = selectedTextColor
    }

    func makeButton(_ item: Item, idx: Int) -> Button {
        Button(
            item: item,
            tap: { [weak self] in self?.tappedItem(item, at: idx) }
        )
    }
}

// MARK: - SegmentedControl.Button

extension SegmentedControl {

    class Button: UIButton {

        var label: UILabel?
        var iconView: UIImageView?

        convenience init(item: Item, tap: (() -> Void)? = nil) {
            let label = UILabel(text: item.title, font: Theme.captionMedium)
            let iconView = UIImageView(image: item.image)
            self.init(type: .custom)
            self.label = label
            self.iconView = iconView
            iconView.isHidden = item.image == nil
            let spacers = [UIView(), UIView()]
            let stack = HStackView([spacers[0], label, iconView, spacers[1]])
            stack.isUserInteractionEnabled = false
            stack.spacing = Padding.half
            stack.translatesAutoresizingMaskIntoConstraints = false
            addSubview(stack)
            stack.constrain(toSuperviewEdges: nil)
            stack.distribution = .fill
            stack.alignment = .center
            stack.constrain([
                iconView.widthAnchor.constraint(equalToConstant: 16),
                iconView.heightAnchor.constraint(equalToConstant: 16),
                spacers[0].widthAnchor.constraint(equalTo: spacers[1].widthAnchor)
            ])
            self.tap = tap
        }
    }
}

// MARK: - SegmentedControl.Item

extension SegmentedControl {

    struct Item {
        let title: String?
        let image: UIImage?

        init(title: String? = nil, image: UIImage? = nil) {
            self.title = title
            self.image = image
        }
    }
}

