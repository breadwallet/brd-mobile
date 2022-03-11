//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCSignInDisplayLogic: class {
    // MARK: Display logic functions
    
    func displayShouldEnableSubmit(viewModel: KYCSignIn.ShouldEnableSubmit.ViewModel)
    func displaySignIn(viewModel: KYCSignIn.SubmitData.ViewModel)
    func displayValidateField(viewModel: KYCSignIn.ValidateField.ViewModel)
    func displayError(viewModel: GenericModels.Error.ViewModel)
}

class KYCSignInViewController: UIViewController, KYCSignInDisplayLogic, UITableViewDelegate, UITableViewDataSource {
    var interactor: KYCSignInBusinessLogic?
    var router: (NSObjectProtocol & KYCSignInRoutingLogic)?
    
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
        let interactor = KYCSignInInteractor()
        let presenter = KYCSignInPresenter()
        let router = KYCSignInRouter()
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
        
        tableView.register(cell: KYCSignInCell.self)
        
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
        
        let dismissFlowButton = UIBarButtonItem(title: "Dismiss", style: .done, target: self, action: #selector(dismissFlow))
        navigationItem.leftBarButtonItem = dismissFlowButton
    }
    
    @objc private func dismissFlow() {
        router?.dismissFlow()
    }
    
    // MARK: View controller functions
    
    func displayShouldEnableSubmit(viewModel: KYCSignIn.ShouldEnableSubmit.ViewModel) {
        guard let index = sections.firstIndex(of: .fields) else { return }
        guard let cell = tableView.cellForRow(at: IndexPath(row: 0, section: index)) as? KYCSignInCell else { return }
        
        let style: KYCButton.ButtonStyle = viewModel.shouldEnable ? .enabled : .disabled
        cell.changeButtonStyle(with: style)
    }
    
    func displaySignIn(viewModel: KYCSignIn.SubmitData.ViewModel) {
        LoadingView.hide()
        
        router?.showKYCTutorialScene()
    }
    
    func displayValidateField(viewModel: KYCSignIn.ValidateField.ViewModel) {
        guard let index = sections.firstIndex(of: .fields) else { return }
        guard let cell = tableView.cellForRow(at: IndexPath(row: 0, section: index)) as? KYCSignInCell else { return }
        
        cell.changeFieldStyle(isViable: viewModel.isViable,
                              for: viewModel.type)
    }
    
    func displayError(viewModel: GenericModels.Error.ViewModel) {
        LoadingView.hide()
        
        let alert = UIAlertController(style: .alert, message: viewModel.error)
        alert.addAction(title: "OK", style: .cancel)
        alert.show(on: self)
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
            return getKYCSignInFieldsCell(indexPath)
            
        }
    }
    
    func getKYCSignInFieldsCell(_ indexPath: IndexPath) -> KYCSignInCell {
        guard let cell = tableView.dequeue(cell: KYCSignInCell.self) else {
            return KYCSignInCell()
        }
        
        cell.didChangeEmailField = { [weak self] text in
            self?.interactor?.executeCheckFieldType(request: .init(text: text, type: .email))
        }
        
        cell.didChangePasswordField = { [weak self] text in
            self?.interactor?.executeCheckFieldType(request: .init(text: text, type: .password))
        }
        
        cell.didTapNextButton = { [weak self] in
            self?.interactor?.executeSignIn(request: .init())
        }
        
        cell.didTapSignUpButton = { [weak self] in
            self?.router?.showKYCSignUpScene()
        }
        
        return cell
    }
}
