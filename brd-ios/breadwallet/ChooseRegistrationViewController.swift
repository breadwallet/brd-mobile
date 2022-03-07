//
//  ChooseRegistrationViewController.swift
//  breadwallet
//
//  Created by Dijana Angelovska on 7.3.22.
//  Copyright (c) 2022 Breadwinner AG. All rights reserved.
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
    }
    
    // MARK: View controller functions
    
}
