//
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol SignInDisplayLogic: class {
    // MARK: Display logic functions
    
    func displayLogin(viewModel: SignIn.LoginData.ViewModel)
    func displayError(viewModel: GenericModels.Error.ViewModel)
}

class SignInViewController: UIViewController, SignInDisplayLogic {
    var interactor: SignInBusinessLogic?
    var router: (NSObjectProtocol & SignInRoutingLogic)?
    
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
        let interactor = SignInInteractor()
        let presenter = SignInPresenter()
        let router = SignInRouter()
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
        stack.spacing = 18
        return stack
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.text = "LOG IN"
        label.textAlignment = .center
        label.textColor = .almostBlack
        label.font = UIFont(name: "AvenirNext-Bold", size: 20)
        return label
    }()
    
    private lazy var emailField: SimpleTextField = {
        let textField = SimpleTextField()
        textField.translatesAutoresizingMaskIntoConstraints = false
        textField.setup(as: .text, textFieldType: .email, title: "EMAIL", customPlaceholder: "Email")
        return textField
    }()
    
    private lazy var passwordField: SimpleTextField = {
        let textField = SimpleTextField()
        textField.translatesAutoresizingMaskIntoConstraints = false
        textField.setup(as: .text, textFieldType: .password, title: "PASSWORD", customPlaceholder: "Password")
        return textField
    }()
    
    private lazy var submitButton: KYCButton = {
        let button = KYCButton()
        button.translatesAutoresizingMaskIntoConstraints = false
        button.setup(as: .normal, title: "SUBMIT")
        return button
    }()
    
    private lazy var signUpLabel: UILabel = {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.font = UIFont(name: "AvenirNext-Regular", size: 16)
        label.textColor = .kycGray2
        label.textAlignment = .center
        let text = "Don’t have an account? Sign Up"
        label.text = text
        let underlineAttriString = NSMutableAttributedString(string: text)
        let range = (text as NSString).range(of: "Sign Up")
             underlineAttriString.addAttribute(NSAttributedString.Key.underlineStyle, value: NSUnderlineStyle.single.rawValue, range: range)
             underlineAttriString.addAttribute(NSAttributedString.Key.font, value: UIFont.init(name: "AvenirNext-Regular", size: 16)!, range: range)
        underlineAttriString.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.vibrantYellow, range: range)
        label.attributedText = underlineAttriString
        label.isUserInteractionEnabled = true
        label.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(signUpTapped(_:))))
        return label
    }()
    
    private lazy var logoImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "FabriikLogo"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private func setupUI() {
        view.backgroundColor = .white
        
        view.addSubview(stack)
        stack.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: C.Sizes.brdLogoHeight).isActive = true
        stack.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: C.Sizes.brdLogoHeight).isActive = true
        stack.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -C.Sizes.brdLogoHeight).isActive = true
        
        stack.addArrangedSubview(titleLabel)
        stack.addArrangedSubview(emailField)
        stack.addArrangedSubview(passwordField)
        stack.addArrangedSubview(submitButton)
        submitButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        
        stack.addArrangedSubview(signUpLabel)
        
        submitButton.didTap = { [weak self] in
            self?.interactor?.login(request: .init(email: self?.emailField.textField.text, password: self?.passwordField.textField.text))
        }
    }
    
    @objc func signUpTapped(_ sender: UIButton?) {
        router?.showSignUp()
    }
    
    @objc fileprivate func backButtonTapped() {
        dismiss(animated: true)
    }
    
    func displayLogin(viewModel: SignIn.LoginData.ViewModel) {
       // displayLogin
    }
    
    func displayError(viewModel: GenericModels.Error.ViewModel) {
        LoadingView.hide()
        let alert = UIAlertController(style: .alert, message: viewModel.error)
        alert.addAction(title: "OK", style: .cancel)
        alert.show(on: self)
    }
}
