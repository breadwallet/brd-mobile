// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCCompleteDisplayLogic: class {
    // MARK: Display logic functions
}

class KYCCompleteViewController: UIViewController, KYCCompleteDisplayLogic, UITableViewDelegate, UITableViewDataSource {
    var interactor: KYCCompleteBusinessLogic?
    var router: (NSObjectProtocol & KYCCompleteRoutingLogic)?
    
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
        let interactor = KYCCompleteInteractor()
        let presenter = KYCCompletePresenter()
        let router = KYCCompleteRouter()
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
        case progress
        case textAndImage
        case buttons
    }
    
    private lazy var roundedView: RoundedView = {
        let roundedView = RoundedView()
        roundedView.translatesAutoresizingMaskIntoConstraints = false
        roundedView.cornerRadius = 10
        roundedView.backgroundColor = .navigationTint
        
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
    
    private let sections: [Section] = [
        .progress,
        .textAndImage,
        .buttons
    ]
    
    // MARK: View lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.register(cell: KYCProgressCell.self)
        tableView.register(cell: KYCTextAndImageCell.self)
        tableView.register(cell: KYCCompleteButtons.self)
        
        view.addSubview(roundedView)
        roundedView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 32).isActive = true
        roundedView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        roundedView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        roundedView.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: 20).isActive = true
        
        roundedView.addSubview(tableView)
        tableView.topAnchor.constraint(equalTo: roundedView.topAnchor).isActive = true
        tableView.leadingAnchor.constraint(equalTo: roundedView.leadingAnchor).isActive = true
        tableView.trailingAnchor.constraint(equalTo: roundedView.trailingAnchor).isActive = true
        tableView.bottomAnchor.constraint(equalTo: roundedView.bottomAnchor, constant: -20).isActive = true
        
        view.backgroundColor = .almostBlack
    }
    
    // MARK: View controller functions
    
    // MARK: - UITableView
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        switch sections[indexPath.section] {
        case .progress:
            return getKYCProgressCell(indexPath)
            
        case .textAndImage:
            return getKYCTextAndImageCell(indexPath)
            
        case .buttons:
            return getKYCCompleteButtons(indexPath)
            
        }
    }
    
    func getKYCProgressCell(_ indexPath: IndexPath) -> KYCProgressCell {
        guard let cell = tableView.dequeue(cell: KYCProgressCell.self) else {
            return KYCProgressCell()
        }
        
        cell.setValues(text: "KYC COMPLETE!", progress: .complete)
        
        return cell
    }
    
    func getKYCTextAndImageCell(_ indexPath: IndexPath) -> KYCTextAndImageCell {
        guard let cell = tableView.dequeue(cell: KYCTextAndImageCell.self) else {
            return KYCTextAndImageCell()
        }
        
        cell.set(text: "Your profile is under review.\nYou will recieve an email with the status of your review in 1-2 business days.",
                 image: UIImage(named: "KYC Complete"))
        
        return cell
    }
    
    func getKYCCompleteButtons(_ indexPath: IndexPath) -> KYCCompleteButtons {
        guard let cell = tableView.dequeue(cell: KYCCompleteButtons.self) else {
            return KYCCompleteButtons()
        }
        
        cell.didTapDoneButton = { [weak self] in
            self?.navigationController?.dismiss(animated: true)
        }
        
        return cell
    }
}
