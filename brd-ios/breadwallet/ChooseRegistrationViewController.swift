//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol ChooseRegistrationDisplayLogic: class {
    // MARK: Display logic functions
}

class ChooseRegistrationViewController: UIViewController, ChooseRegistrationDisplayLogic {
    var interactor: ChooseRegistrationBusinessLogic?
    var router: (NSObjectProtocol & ChooseRegistrationRoutingLogic)?
    
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
        let interactor = ChooseRegistrationInteractor()
        let presenter = ChooseRegistrationPresenter()
        let router = ChooseRegistrationRouter()
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
    
    // MARK: View lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let backItem = UIBarButtonItem(title: "<", style: .plain, target: self, action: #selector(backButtonTapped))
        navigationController?.navigationBar.topItem?.leftBarButtonItem = backItem
        
        setupUI()
    }
    
    // MARK: View controller functions
    
    private lazy var stack: UIStackView = {
        let stack = UIStackView()
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.axis = .vertical
        stack.spacing = 50
        return stack
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.text = "ONE WALLET"
        label.textAlignment = .center
        label.textColor = .vibrantYellow
        label.font = UIFont(name: "AvenirNext-Medium", size: 22)
        return label
    }()
    
    private lazy var logoImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "FabriikLogo"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "FabriikLogoIcon"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.contentMode = .scaleAspectFit
        imageView.tintColor = .vibrantYellow
        return imageView
    }()
    
    private lazy var buttonsStack: UIStackView = {
        let stack = UIStackView()
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.distribution = .fillEqually
        stack.spacing = 20
        return stack
    }()
    
    private lazy var signInButton: KYCButton = {
        let nextButton = KYCButton()
        nextButton.translatesAutoresizingMaskIntoConstraints = false
        nextButton.setup(as: .normal, title: "SIGN IN")
        
        return nextButton
    }()
    
    private lazy var signUpButton: KYCButton = {
        let nextButton = KYCButton()
        nextButton.translatesAutoresizingMaskIntoConstraints = false
        nextButton.setup(as: .almostBlack, title: "SIGN UP")
        return nextButton
    }()
    
    private func setupUI() {
        view.backgroundColor = .almostBlack
        
        view.addSubview(stack)
        stack.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: C.Sizes.brdLogoHeight).isActive = true
        stack.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -C.Sizes.brdLogoHeight).isActive = true
        stack.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: C.Sizes.brdLogoHeight).isActive = true
        stack.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -C.Sizes.brdLogoHeight).isActive = true
        
        stack.addArrangedSubview(titleLabel)
        stack.addArrangedSubview(imageView)
        imageView.heightAnchor.constraint(equalToConstant: 270).isActive = true
        stack.addArrangedSubview(logoImageView)
        
        stack.addArrangedSubview(buttonsStack)
        buttonsStack.addArrangedSubview(signInButton)
        buttonsStack.addArrangedSubview(signUpButton)
        
        signInButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
    }
    
    @objc fileprivate func backButtonTapped() {
        dismiss(animated: true)
    }
}
