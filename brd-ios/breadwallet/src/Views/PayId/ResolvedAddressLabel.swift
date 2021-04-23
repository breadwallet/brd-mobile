// 
//  PayIdLabel.swift
//  breadwallet
//
//  Created by Adrian Corscadden on 2020-06-08.
//  Copyright © 2020 Breadwinner AG. All rights reserved.
//
//  See the LICENSE file at the project root for license information.
//

import UIKit

class ResolvedAddressLabel: UIView {
    
    private let imageHeight: CGFloat = 22.0
    private let image = UIImageView()
    private let addressLabel = UILabel(font: .customBody(size: 12.0), color: .lightText)
    private var widthConstraint: NSLayoutConstraint?
    
    var type: ResolvableType? {
        didSet {
            guard let iconName = type?.iconName else { image.image = nil; return }
            image.image = UIImage(named: iconName)
            if widthConstraint != nil {
                NSLayoutConstraint.deactivate([widthConstraint!])
            }
            
            if let size = image.image?.size {
                let aspectRatio = size.width/size.height
                let width = aspectRatio * imageHeight
                widthConstraint = image.widthAnchor.constraint(equalToConstant: width)
                NSLayoutConstraint.activate([widthConstraint!])
            }
            self.image.transform = .identity
            
            let duration = 0.2
            let scale: CGFloat = 1.2
            
            UIView.spring(duration, animations: {
                self.image.transform = CGAffineTransform(scaleX: scale, y: scale)
            }, completion: { _ in
                UIView.spring(duration, animations: {
                    self.image.transform = .identity
                }, completion: { _ in })
            })
        }
    }
    
    var address: String? {
        didSet {
            addressLabel.text = address
            addressLabel.lineBreakMode = .byTruncatingMiddle
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(image)
        addSubview(addressLabel)
        backgroundColor = .blue
        image.constrain([
            image.leadingAnchor.constraint(equalTo: leadingAnchor),
            image.topAnchor.constraint(equalTo: topAnchor),
            image.heightAnchor.constraint(equalToConstant: imageHeight)
        ])
        image.contentMode = .scaleAspectFit
        addressLabel.constrain([
            addressLabel.leadingAnchor.constraint(equalTo: leadingAnchor),
            addressLabel.topAnchor.constraint(equalTo: image.bottomAnchor, constant: C.padding[2]),
            addressLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -C.padding[10])
        ])
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}
