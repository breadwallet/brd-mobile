//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCConfirmEmailDisplayLogic: class {
    // MARK: Display logic functions
    
    func displayShouldEnableConfirm(viewModel: KYCConfirmEmail.ShouldEnableConfirm.ViewModel)
    func displayValidateField(viewModel: KYCConfirmEmail.ValidateField.ViewModel)
}

class KYCConfirmEmailViewController: UIViewController, KYCConfirmEmailDisplayLogic, UITableViewDelegate, UITableViewDataSource {
    var interactor: KYCConfirmEmailBusinessLogic?
    var router: (NSObjectProtocol & KYCConfirmEmailRoutingLogic)?
    
    // MARK: Object lifecycle
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setup()
    }
    
    // MARK: Setup
    private func setup() {
        let viewController = self
        let interactor = KYCConfirmEmailInteractor()
        let presenter = KYCConfirmEmailPresenter()
        let router = KYCConfirmEmailRouter()
        viewController.interactor = interactor
        viewController.router = router
        interactor.presenter = presenter
        presenter.viewController = viewController
        router.viewController = viewController
        router.dataStore = interactor
    }
    
    // MARK: Routing
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let scene = segue.identifier {
            let selector = NSSelectorFromString("routeTo\(scene)WithSegue:")
            if let router = router, router.responds(to: selector) {
                router.perform(selector, with: segue)
            }
        }
    }
    
    // MARK: - Properties
    
    enum Section {
        case fields
    }
    
    private lazy var roundedView: RoundedView = {
        let roundedView = RoundedView()
        roundedView.translatesAutoresizingMaskIntoConstraints = false
        roundedView.cornerRadius = 10
        roundedView.backgroundColor = .kycCompletelyWhite
        
        return roundedView
    }()
    
    private lazy var tableView: UITableView = {
        var tableView = UITableView()
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.setupDefault()
        tableView.allowsSelection = false
        tableView.delegate = self
        tableView.dataSource = self
        
        return tableView
    }()
    
    private lazy var footerView: KYCFooterView = {
        let footerView = KYCFooterView()
        footerView.translatesAutoresizingMaskIntoConstraints = false
        
        return footerView
    }()
    
    private let sections: [Section] = [
        .fields
    ]
    
    // MARK: View lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.register(cell: KYCConfirmEmailCell.self)
        
        view.addSubview(roundedView)
        roundedView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 32).isActive = true
        roundedView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        roundedView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        roundedView.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: 20).isActive = true
        
        roundedView.addSubview(footerView)
        footerView.leadingAnchor.constraint(equalTo: roundedView.leadingAnchor).isActive = true
        footerView.trailingAnchor.constraint(equalTo: roundedView.trailingAnchor).isActive = true
        footerView.bottomAnchor.constraint(equalTo: roundedView.bottomAnchor, constant: -40).isActive = true
        footerView.heightAnchor.constraint(equalToConstant: 40).isActive = true
        
        roundedView.addSubview(tableView)
        tableView.topAnchor.constraint(equalTo: roundedView.topAnchor).isActive = true
        tableView.leadingAnchor.constraint(equalTo: roundedView.leadingAnchor).isActive = true
        tableView.trailingAnchor.constraint(equalTo: roundedView.trailingAnchor).isActive = true
        tableView.bottomAnchor.constraint(equalTo: roundedView.bottomAnchor, constant: -20).isActive = true
        
        view.backgroundColor = .almostBlack
    }
    
    // MARK: View controller functions
    
    func displayShouldEnableConfirm(viewModel: KYCConfirmEmail.ShouldEnableConfirm.ViewModel) {
        guard let index = sections.firstIndex(of: .fields) else { return }
        guard let cell = tableView.cellForRow(at: IndexPath(row: 0, section: index)) as? KYCConfirmEmailCell else { return }
        
        let style: KYCButton.ButtonStyle = viewModel.shouldEnable ? .enabled : .disabled
        cell.changeButtonStyle(with: style)
    }
    
    func displayValidateField(viewModel: KYCConfirmEmail.ValidateField.ViewModel) {
        guard let index = sections.firstIndex(of: .fields) else { return }
        guard let cell = tableView.cellForRow(at: IndexPath(row: 0, section: index)) as? KYCConfirmEmailCell else { return }
        
        cell.changeFieldStyle(isViable: viewModel.isViable)
    }
    
    // MARK: - UITableView
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        switch sections[indexPath.section] {
        case .fields:
            return getKYCSConfirmEmailFieldsCell(indexPath)
        }
    }
    
    func getKYCSConfirmEmailFieldsCell(_ indexPath: IndexPath) -> KYCConfirmEmailCell {
        guard let cell = tableView.dequeue(cell: KYCConfirmEmailCell.self) else {
            return KYCConfirmEmailCell()
        }
        
        cell.didChangeConfirmationCodeField = { [weak self] text in
            self?.interactor?.executeCheckFieldType(request: .init(text: text,
                                                                   type: .code))
        }
        
        cell.didTapConfirmButton = { [weak self] in
            self?.interactor?.executeConfirmData(request: .init())
        }
        
        return cell
    }
}
