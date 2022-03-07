// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

class KYCCameraCell: UITableViewCell, KYCCameraViewDelegate {
    private lazy var cameraContainerView: RoundedView = {
        let cameraContainerView = RoundedView()
        cameraContainerView.translatesAutoresizingMaskIntoConstraints = false
        cameraContainerView.cornerRadius = 15
        cameraContainerView.backgroundColor = .kycGray1
        
        return cameraContainerView
    }()
    
    private lazy var buttonsStackView: UIStackView = {
        let buttonsStackView = UIStackView()
        buttonsStackView.translatesAutoresizingMaskIntoConstraints = false
        buttonsStackView.alignment = .center
        buttonsStackView.distribution = .fillProportionally
        buttonsStackView.axis = .horizontal
        buttonsStackView.spacing = 16
        
        return buttonsStackView
    }()
    
    private lazy var flipCameraButton: UIButton = {
        let flipCameraButton = UIButton()
        flipCameraButton.translatesAutoresizingMaskIntoConstraints = false
        flipCameraButton.setImage(UIImage(named: "Flip Camera"), for: .normal)
        flipCameraButton.addTarget(self, action: #selector(flipAction), for: .touchUpInside)
        
        return flipCameraButton
    }()
    
    private lazy var retryButton: UIButton = {
        let retryButton = UIButton()
        retryButton.translatesAutoresizingMaskIntoConstraints = false
        retryButton.setImage(UIImage(named: "KYC Camera Retry Enabled"), for: .normal)
        retryButton.setImage(UIImage(named: "KYC Camera Retry"), for: .disabled)
        retryButton.imageView?.contentMode = .scaleAspectFit
        retryButton.addTarget(self, action: #selector(retryAction), for: .touchUpInside)
        
        return retryButton
    }()
    
    private lazy var shutterButton: UIButton = {
        let shutterButton = UIButton()
        shutterButton.translatesAutoresizingMaskIntoConstraints = false
        shutterButton.setImage(UIImage(named: "KYC Camera Shutter"), for: .normal)
        shutterButton.imageView?.contentMode = .scaleAspectFit
        shutterButton.addTarget(self, action: #selector(captureAction), for: .touchUpInside)
        
        return shutterButton
    }()
    
    private lazy var nextButton: UIButton = {
        let nextButton = UIButton()
        nextButton.translatesAutoresizingMaskIntoConstraints = false
        nextButton.setImage(UIImage(named: "KYC Camera Next Enabled"), for: .normal)
        nextButton.setImage(UIImage(named: "KYC Camera Next"), for: .disabled)
        nextButton.imageView?.contentMode = .scaleAspectFit
        nextButton.addTarget(self, action: #selector(nextAction), for: .touchUpInside)
        
        return nextButton
    }()
    
    private lazy var cameraView: KYCCameraView = {
        let cameraView = KYCCameraView()
        cameraView.translatesAutoresizingMaskIntoConstraints = false
        
        return cameraView
    }()
    
    private var image = UIImage()
    
    var didTapNextButton: ((UIImage) -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        addSubview(cameraContainerView)
        cameraContainerView.topAnchor.constraint(equalTo: topAnchor, constant: 26).isActive = true
        cameraContainerView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 32).isActive = true
        cameraContainerView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -32).isActive = true
        cameraContainerView.heightAnchor.constraint(equalTo: cameraContainerView.widthAnchor).isActive = true
        
        cameraContainerView.addSubview(cameraView)
        cameraView.topAnchor.constraint(equalTo: cameraContainerView.topAnchor).isActive = true
        cameraView.bottomAnchor.constraint(equalTo: cameraContainerView.bottomAnchor).isActive = true
        cameraView.leadingAnchor.constraint(equalTo: cameraContainerView.leadingAnchor).isActive = true
        cameraView.trailingAnchor.constraint(equalTo: cameraContainerView.trailingAnchor).isActive = true
        
        cameraView.delegate = self
        cameraView.startPreview(requestPermissionIfNeeded: true)
        
        cameraContainerView.addSubview(flipCameraButton)
        flipCameraButton.topAnchor.constraint(equalTo: cameraContainerView.topAnchor, constant: 12).isActive = true
        flipCameraButton.trailingAnchor.constraint(equalTo: cameraContainerView.trailingAnchor, constant: -12).isActive = true
        flipCameraButton.widthAnchor.constraint(equalToConstant: 24).isActive = true
        flipCameraButton.heightAnchor.constraint(equalToConstant: 24).isActive = true
        
        addSubview(buttonsStackView)
        buttonsStackView.topAnchor.constraint(equalTo: cameraContainerView.bottomAnchor, constant: 16).isActive = true
        buttonsStackView.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -16).isActive = true
        buttonsStackView.leadingAnchor.constraint(equalTo: cameraContainerView.leadingAnchor).isActive = true
        buttonsStackView.trailingAnchor.constraint(equalTo: cameraContainerView.trailingAnchor).isActive = true
        buttonsStackView.heightAnchor.constraint(equalToConstant: 76).isActive = true
        
        buttonsStackView.addArrangedSubview(retryButton)
        buttonsStackView.addArrangedSubview(shutterButton)
        buttonsStackView.addArrangedSubview(nextButton)
        
        changeCameraControlState(isEnabled: true)
    }
    
    @objc func retryAction() {
        flipCameraButton.isEnabled = true
        
        cameraView.startPreview(requestPermissionIfNeeded: true)
        
        changeCameraControlState(isEnabled: true)
    }
    
    @objc func captureAction() {
        flipCameraButton.isEnabled = false
        
        cameraView.capturePhoto()
        
        changeCameraControlState(isEnabled: false)
    }
    
    @objc func flipAction() {
        cameraView.cameraType = cameraView.cameraType == .front ? .back : .front
    }
    
    @objc func nextAction() {
        flipCameraButton.isEnabled = true
        
        didTapNextButton?(image)
    }
    
    func stopCamera() {
        flipCameraButton.isEnabled = false
        
        cameraView.stopPreview()
    }
    
    func cameraViewDidCaptureImage(image: UIImage, cameraView: KYCCameraView) {
        flipCameraButton.isEnabled = false
        
        cameraView.stopPreview()
        
        self.image = image
    }
    
    func cameraViewDidFailToCaptureImage(error: Error, cameraView: KYCCameraView) {
        disableCameraControl()
    }
    
    func activateSelfieCamera() {
        cameraView.cameraType = .front
    }
    
    func changeCameraControlState(isEnabled: Bool) {
        retryButton.isEnabled = !isEnabled
        shutterButton.isEnabled = isEnabled
        nextButton.isEnabled = !isEnabled
        flipCameraButton.isEnabled = isEnabled
    }
    
    private func disableCameraControl() {
        retryButton.isEnabled = false
        shutterButton.isEnabled = false
        nextButton.isEnabled = false
        flipCameraButton.isEnabled = false
    }
}
