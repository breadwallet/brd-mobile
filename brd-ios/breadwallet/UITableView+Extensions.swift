// 
// Created by Equaleyes Solutions Ltd
// 

import UIKit

public protocol Identifiable {
    static var identifier: String { get }
    static var className: AnyClass { get }
}

extension Identifiable where Self: UIView {
    public static var identifier: String {
        return String(describing: self)
    }
    
    public static var className: AnyClass {
        return Self.self
    }
}

extension UITableView {
    public func register<T: Identifiable>(_ cell: T.Type) {
        register(cell.className, forCellReuseIdentifier: cell.identifier)
    }
    
    public func dequeueReusableCell<T: Identifiable>(for indexPath: IndexPath) -> T? {
        return dequeueReusableCell(withIdentifier: T.identifier, for: indexPath) as? T
    }
    
    public func registerHeaderFooter<T: Identifiable>(_ cell: T.Type) {
        register(cell.className, forHeaderFooterViewReuseIdentifier: cell.identifier)
    }
    
    public func dequeueHeaderFooter<T: Identifiable>() -> T? {
        return dequeueReusableHeaderFooterView(withIdentifier: T.identifier) as? T
    }
}

extension UICollectionView {
    func register<T: UICollectionViewCell>(cells: [T.Type]) {
        cells.forEach { self.register(cell: $0) }
    }

    func register<T: UICollectionViewCell>(cell: T.Type) {
        let className = String(describing: cell)
        registerNib(named: className, forCellReuseIdentifier: className)
    }

    func registerNib(named: String, forCellReuseIdentifier reuseIdentifier: String) {
        let nib = UINib(nibName: named, bundle: nil)
        register(nib, forCellWithReuseIdentifier: reuseIdentifier)
    }

    func dequeue<T: UICollectionViewCell>(cell: T.Type, indexPath: IndexPath) -> T? {
        let className = String(describing: T.self)
        return dequeueReusableCell(withReuseIdentifier: className, for: indexPath) as? T
    }
}

protocol CellDequeueable {
    static var identifier: String { get }
}

protocol CellFromClass {
    static var className: AnyClass { get }
}

extension CellFromClass where Self: AnyObject {
    static var className: AnyClass {
        return Self.self
    }
}
extension UITableView {
    func register<T: UITableViewCell>(cells: [T.Type]) {
        cells.forEach { self.register(cell: $0) }
    }
    
    func register<T: UITableViewCell>(cell: T.Type) {
        let className = String(describing: cell)
        registerNib(named: className, forCellReuseIdentifier: className)
    }
    
    func register(_ cell: (CellFromClass & CellDequeueable).Type) {
        register(cell.className, forCellReuseIdentifier: cell.identifier)
    }
    
    func registerHeaderFooter<T: UIView>(view: T.Type) {
        let className = String(describing: view)
        let nib = UINib(nibName: className, bundle: nil)
        register(nib, forHeaderFooterViewReuseIdentifier: className)
    }
    
    func registerNib(named: String, forCellReuseIdentifier reuseIdentifier: String) {
        let nib = UINib(nibName: named, bundle: nil)
        register(nib, forCellReuseIdentifier: reuseIdentifier)
    }
    
    func dequeue<T: UITableViewCell>(cell: T.Type) -> T? {
        let className = String(describing: T.self)
        return dequeueReusableCell(withIdentifier: className) as? T
    }
    
    func dequeueReusableHeaderFooterView<T: UIView>(view: T.Type) -> T? {
        let className = String(describing: T.self)
        return dequeueReusableHeaderFooterView(withIdentifier: className) as? T
    }
    
    func resizeTableViewHeader() {
        guard let headerView = tableHeaderView else { return }
        tableHeaderView = resizeTableViewHeaderFooterView(view: headerView)
        layoutIfNeeded()
    }
    
    func resizeTableViewFooter() {
        guard let footerView = tableFooterView else { return }
        tableFooterView = resizeTableViewHeaderFooterView(view: footerView)
        layoutIfNeeded()
    }
    
    fileprivate func resizeTableViewHeaderFooterView(view: UIView) -> UIView {
        let size = view.systemLayoutSizeFitting(UIView.layoutFittingCompressedSize)
        if view.frame.size.height != size.height {
            view.frame.size.height = size.height
        }
        return view
    }
    
    func dequeueReusableCell<T: CellDequeueable>(for indexPath: IndexPath) -> T? {
        return dequeueReusableCell(withIdentifier: T.identifier, for: indexPath) as? T
    }
}

// MARK: - UI Tweaks

extension UITableView {
    func emptyHeaderFooterView() {
        tableHeaderView = UIView(frame: CGRect(origin: .zero,
                                               size: CGSize(width: 0,
                                                            height: CGFloat.leastNonzeroMagnitude)))
        tableFooterView = UIView(frame: CGRect(origin: .zero,
                                               size: CGSize(width: 0,
                                                            height: CGFloat.leastNonzeroMagnitude)))
    }
    
    func setupDefault() {
        separatorStyle = .none
        delaysContentTouches = false
        keyboardDismissMode = .interactive
        estimatedRowHeight = UITableView.automaticDimension
        rowHeight = UITableView.automaticDimension
        backgroundColor = .clear
    }
}
