// 
// Created by Equaleyes Solutions Ltd
// 

import UIKit

class KYCTutorial2CollectionViewCell: KYCTutorialBaseCell {
    @IBOutlet private var topLabel: UILabel!
    @IBOutlet private var imageView: UIImageView!
    @IBOutlet private var nextButton: KYCButton!
    
    var didTapCloseButton: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        // swiftlint:disable line_length
        topLabel.text = "Please have a valid form of government issued ID such as a passport or drivers lisence ready.\nYou will also be asked to take a selfie, so before you start make sure that you are in a well-lit area."
        topLabel.textColor = .kycCompletelyWhite
        
        imageView.image = UIImage(named: "Tutorial2")
        
        nextButton.setup(as: .enabled, title: "BEGIN")
        nextButton.didTap = { [weak self] in
            self?.didTapCloseButton?()
        }
    }
}
