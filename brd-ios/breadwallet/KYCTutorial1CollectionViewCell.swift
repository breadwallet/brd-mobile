// 
// Created by Equaleyes Solutions Ltd
// 

import UIKit

class KYCTutorial1CollectionViewCell: KYCTutorialBaseCell {
    @IBOutlet private var topLabel: UILabel!
    @IBOutlet private var imageView: UIImageView!
    @IBOutlet private var closeButton: UIButton!
    @IBOutlet private var nextButton: UIButton!
    
    var didTapCloseButton: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        topLabel.text = "As a regulated financial services company, we are required to identify the users on our platform."
        topLabel.textColor = .kycCompletelyWhite
        
        imageView.image = UIImage(named: "Tutorial1")
    }
    
    @IBAction func closeButtonTapped(_ sender: Any) {
        didTapCloseButton?()
    }
    
    @IBAction func nextButtonTapped(_ sender: UIButton) {
        nextTapped?()
    }
}
