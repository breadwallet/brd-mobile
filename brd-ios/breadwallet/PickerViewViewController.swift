// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

extension UIAlertController {
    /// Add a picker view
    ///
    /// - Parameters:
    ///   - values: values for picker view
    ///   - selection: initial selection of picker view
    ///   - action: action for selected value of picker view
    @discardableResult
    public func addPickerView(values: PickerViewViewController.Values,
                              selection: PickerViewViewController.Index? = nil) -> PickerViewViewController {
        let pickerView = PickerViewViewController(values: values, selection: selection)
        set(vc: pickerView, height: 216)
        return pickerView
    }
    
    /// Set alert's content viewController
    ///
    /// - Parameters:
    ///   - vc: ViewController
    ///   - height: height of content viewController
    public func set(vc: UIViewController?, height: CGFloat? = nil) {
        guard let vc = vc else { return }
        setValue(vc, forKey: "contentViewController")
        if let height = height {
            vc.preferredContentSize.height = height
            preferredContentSize.height = height
        }
    }
}

final public class PickerViewViewController: UIViewController {
    public class PickerIndex: Codable {
        let column: Int
        let row: Int
        
        init(column: Int, row: Int) {
            self.column = column
            self.row = row
        }
    }
    
    public typealias Values = [[String]]
    public typealias Index = PickerIndex
    public typealias Action = (_ vc: UIViewController, _ picker: UIPickerView, _ index: Index?, _ values: Values) -> Void
    
    fileprivate var values: Values = [[]]
    fileprivate var selection = Index(column: 0, row: 0)
    fileprivate lazy var pickerView: UIPickerView = {
        return UIPickerView()
    }()
    
    public init(values: Values, selection: Index? = nil) {
        super.init(nibName: nil, bundle: nil)
        
        self.values = values
        self.selection = selection ?? Index(column: 0, row: 0)
    }
    
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    /**
     Displays the PickerViewController from the passed `viewController`. Note: For iPad support you must pass `sourceView` too.
     - parameters:
     - viewController: Pass the `viewController` on top of which the PickerViewController will be presented.
     - sourceView: Required for iPad support. Pass the View that triggered the action (Button, UITableViewCell etc.)
     - values: Values to be displayed and picked from
     - selection: Index of current selected item - makes the pickerView to scroll to that index
     - action: Callback on when the users taps Confirm
     */
    static func show(on viewController: UIViewController,
                     sourceView: UIView? = nil,
                     title: String? = nil,
                     values: PickerViewViewController.Values,
                     selection: PickerViewViewController.Index? = nil,
                     confirmAlertTitle: String = "Confirm",
                     cancelAlertTitle: String = "Cancel",
                     action: PickerViewViewController.Action?) {
        let alert = UIAlertController(title: title, message: nil, preferredStyle: .actionSheet)
        alert.view.tintColor = .black
        
        if let sourceView = sourceView {
            alert.popoverPresentationController?.sourceView = sourceView
            alert.popoverPresentationController?.sourceRect = sourceView.frame
        }
        
        let pickerController = alert.addPickerView(values: values, selection: selection)
        
        alert.addAction(UIAlertAction(title: confirmAlertTitle, style: .default, handler: { _ in
            action?(pickerController, pickerController.pickerView, pickerController.selection, values)
        }))
        
        alert.addAction(UIAlertAction(title: cancelAlertTitle, style: .cancel))
        
        alert.view.layoutIfNeeded()
        
        viewController.present(alert, animated: true)
    }
    
    override public func loadView() {
        let wrapper = UIView()
        wrapper.addSubview(pickerView)
        pickerView.translatesAutoresizingMaskIntoConstraints = false
        pickerView.topAnchor.constraint(equalTo: wrapper.topAnchor).isActive = true
        pickerView.bottomAnchor.constraint(equalTo: wrapper.bottomAnchor).isActive = true
        pickerView.leadingAnchor.constraint(equalTo: wrapper.leadingAnchor).isActive = true
        pickerView.trailingAnchor.constraint(equalTo: wrapper.trailingAnchor).isActive = true
        
        view = wrapper
    }
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        
        pickerView.dataSource = self
        pickerView.delegate = self
    }
    
    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        
        if values.count > selection.column, values[selection.column].count > selection.row {
            pickerView.selectRow(selection.row, inComponent: selection.column, animated: true)
        }
    }
}

extension PickerViewViewController: UIPickerViewDataSource, UIPickerViewDelegate {
    public func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return values.count
    }
    
    public func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return values[component].count
    }
    
    public func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return values[component][row]
    }
    
    public func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        selection = Index(column: component, row: row)
    }
    
    public func pickerView(_ pickerView: UIPickerView, viewForRow row: Int, forComponent component: Int, reusing view: UIView?) -> UIView {
        let label = UILabel(frame: CGRect(origin: .zero, size: CGSize(width: pickerView.frame.width - 20, height: 44)))
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 22)
        label.adjustsFontSizeToFitWidth = true
        label.minimumScaleFactor = 0.5
        label.text = values[component][row]
        
        return label
    }
}
