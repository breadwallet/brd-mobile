// 
// Created by Equaleyes Solutions Ltd
// 

import UIKit

protocol KYCTutorialDisplayLogic: class {
    // MARK: Display logic functions
    
    func displayTutorialPages(viewModel: KYCTutorial.FetchTutorialPages.ViewModel)
    func displayNextTutorial(viewModel: KYCTutorial.HandleTutorialPaging.ViewModel)
}

class KYCTutorialViewController: UIViewController, KYCTutorialDisplayLogic, UICollectionViewDataSource,
                                 UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    var interactor: KYCTutorialBusinessLogic?
    var router: (NSObjectProtocol & KYCTutorialRoutingLogic)?
    
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
        let interactor = KYCTutorialInteractor()
        let presenter = KYCTutorialPresenter()
        let router = KYCTutorialRouter()
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
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.isPagingEnabled = true
        collectionView.alwaysBounceHorizontal = false
        collectionView.alwaysBounceVertical = false
        collectionView.showsHorizontalScrollIndicator = false
        collectionView.showsVerticalScrollIndicator = false
        collectionView.backgroundColor = .clear
        
        return collectionView
    }()
    
    private lazy var pageControl: UIPageControl = {
        let pageControl = UIPageControl()
        pageControl.translatesAutoresizingMaskIntoConstraints = false
        pageControl.isUserInteractionEnabled = false
        pageControl.currentPageIndicatorTintColor = .vibrantYellow
        
        return pageControl
    }()
    
    private var nextPage = 0
    private var tutorialPagesIdentifiers = [String]()
    
    // MARK: View lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        
        collectionView.register(cell: KYCTutorial1CollectionViewCell.self)
        collectionView.register(cell: KYCTutorial2CollectionViewCell.self)
        
        view.addSubview(collectionView)
        collectionView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor).isActive = true
        collectionView.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor).isActive = true
        collectionView.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor).isActive = true
        
        view.addSubview(pageControl)
        pageControl.bottomAnchor.constraint(equalTo: collectionView.bottomAnchor, constant: -72).isActive = true
        pageControl.leadingAnchor.constraint(equalTo: collectionView.leadingAnchor).isActive = true
        pageControl.trailingAnchor.constraint(equalTo: collectionView.trailingAnchor).isActive = true
        
        view.backgroundColor = .almostBlack
        
        interactor?.fetchTutorialPages(request: .init())
    }
    
    func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        nextPage = indexPath.row
        pageControl.currentPage = nextPage
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: collectionView.frame.width,
                      height: collectionView.frame.height)
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return tutorialPagesIdentifiers.count
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let reuseIdentifier = tutorialPagesIdentifiers[indexPath.row]
        guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: reuseIdentifier,
                                                            for: indexPath) as? KYCTutorialBaseCell else {
            return UICollectionViewCell()
        }
        
        switch cell {
        case is KYCTutorial1CollectionViewCell:
            (cell as? KYCTutorial1CollectionViewCell)?.didTapCloseButton = { [weak self] in
                self?.router?.dismissFlow()
            }
            
        case is KYCTutorial2CollectionViewCell:
            (cell as? KYCTutorial2CollectionViewCell)?.didTapCloseButton = { [weak self] in
                self?.router?.showKYCAddressScene()
            }
            
        default:
            break
        }
        
        cell.nextTapped = { [weak self] in
            guard let self = self else { return }
            self.interactor?.nextTutorial(request: .init(row: indexPath.row,
                                                         pageCount: self.tutorialPagesIdentifiers.count))
        }
        
        return cell
    }
    
    func displayTutorialPages(viewModel: KYCTutorial.FetchTutorialPages.ViewModel) {
        tutorialPagesIdentifiers = viewModel.tutorialPagesIdentifiers
        collectionView.reloadData()
        
        pageControl.numberOfPages = tutorialPagesIdentifiers.count
    }
    
    func displayNextTutorial(viewModel: KYCTutorial.HandleTutorialPaging.ViewModel) {
        nextPage = viewModel.nextPage
        pageControl.currentPage = nextPage
        collectionView.isPagingEnabled = false
        collectionView.scrollToItem(at: IndexPath(item: nextPage, section: 0),
                                    at: .right,
                                    animated: true)
        collectionView.isPagingEnabled = true
    }
}
