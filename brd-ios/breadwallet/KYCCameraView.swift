// 
// Created by Equaleyes Solutions Ltd
//

import AVFoundation
import UIKit

public protocol KYCCameraViewDelegate: AnyObject {
    func cameraViewDidCaptureImage(image: UIImage, cameraView: KYCCameraView)
    func cameraViewDidFailToCaptureImage(error: Error, cameraView: KYCCameraView)
}

public class KYCCameraView: UIView {
    public enum CameraType {
        case front
        case back
    }
    
    public weak var delegate: KYCCameraViewDelegate?
    
    public var cameraType: CameraType = .back {
        didSet {
            if self.cameraType != oldValue {
                updateForCameraType()
            }
        }
    }
    
    public var authorizedForCapture: Bool {
        return AVCaptureDevice.authorizationStatus(for: .video) == .authorized
    }
    
    private var frontCameraDeviceInput: AVCaptureDeviceInput?
    private var backCameraDeviceInput: AVCaptureDeviceInput?
    private var captureSession: AVCaptureSession?
    private var photoOutput: AVCapturePhotoOutput?
    private var cameraPreviewLayer: AVCaptureVideoPreviewLayer?
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        backgroundColor = .black
        
        guard authorizedForCapture else { return }
        setupCaptureSession()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        guard let cameraPreviewLayer = cameraPreviewLayer else { return }
        cameraPreviewLayer.frame = bounds
        cameraPreviewLayer.connection?.videoOrientation = .portrait
    }
    
    private func setupCaptureSession() {
        guard let frontCameraDevice = AVCaptureDevice.default(.builtInWideAngleCamera,
                                                              for: .video,
                                                              position: .front) else { return }
        guard let backCameraDevice = AVCaptureDevice.default(.builtInWideAngleCamera,
                                                             for: .video,
                                                             position: .back) else { return }
        
        guard let frontCameraDeviceInput = try? AVCaptureDeviceInput(device: frontCameraDevice) else { return }
        guard let backCameraDeviceInput = try? AVCaptureDeviceInput(device: backCameraDevice) else { return }
        
        self.frontCameraDeviceInput = frontCameraDeviceInput
        self.backCameraDeviceInput = backCameraDeviceInput
        
        captureSession = AVCaptureSession()
        captureSession?.sessionPreset = AVCaptureSession.Preset.photo
        updateForCameraType()
        
        photoOutput = AVCapturePhotoOutput()
        photoOutput?.isHighResolutionCaptureEnabled = true
        
        guard let photoOutput = photoOutput, let captureSession = captureSession else { return }
        captureSession.addOutput(photoOutput)
        
        cameraPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        cameraPreviewLayer?.videoGravity = AVLayerVideoGravity.resizeAspectFill
        cameraPreviewLayer?.frame = bounds
        
        guard let cameraPreviewLayer = cameraPreviewLayer else { return }
        layer.addSublayer(cameraPreviewLayer)
    }
    
    private func updateForCameraType() {
        guard let captureSession = captureSession else { return }
        guard let frontCameraDeviceInput = frontCameraDeviceInput else { return }
        guard let backCameraDeviceInput = backCameraDeviceInput else { return }
        
        switch cameraType {
        case .front:
            if !captureSession.inputs.isEmpty {
                captureSession.removeInput(backCameraDeviceInput)
            }
            
            captureSession.addInput(frontCameraDeviceInput)
            
        case .back:
            if !captureSession.inputs.isEmpty {
                captureSession.removeInput(frontCameraDeviceInput)
            }
            
            captureSession.addInput(backCameraDeviceInput)
            
        }
    }
    
    public func startPreview(requestPermissionIfNeeded: Bool = true) {
        if authorizedForCapture {
            if captureSession == nil {
                setupCaptureSession()
            }
            
            captureSession?.startRunning()
            
        } else if requestPermissionIfNeeded {
            requestCapturePermission { [unowned self] permissionGranted in
                guard permissionGranted else { return }
                self.captureSession?.startRunning()
            }
            
        }
    }
    
    public func stopPreview() {
        captureSession?.stopRunning()
    }
    
    public func capturePhoto(imageStabilization: Bool = true, flashMode: AVCaptureDevice.FlashMode = .auto) {
        guard let photoOutput = self.photoOutput else { return }
        
        let captureSettings = AVCapturePhotoSettings(format: [ AVVideoCodecKey: AVVideoCodecType.jpeg ])
        
        if cameraType == .back {
            captureSettings.flashMode = flashMode
        }
        
        captureSettings.isHighResolutionPhotoEnabled = true
        
        photoOutput.capturePhoto(with: captureSettings, delegate: self)
    }
}

private extension KYCCameraView {
    private func requestCapturePermission(completion: @escaping (_ granted: Bool) -> Void) {
        AVCaptureDevice.requestAccess(for: AVMediaType.video) { permissionGranted in
            DispatchQueue.main.async {
                completion(permissionGranted)
            }
        }
    }
}

extension KYCCameraView: AVCapturePhotoCaptureDelegate {
    public func photoOutput(_ captureOutput: AVCapturePhotoOutput,
                            didFinishProcessingPhoto photoSampleBuffer: CMSampleBuffer?,
                            previewPhoto previewPhotoSampleBuffer: CMSampleBuffer?,
                            resolvedSettings: AVCaptureResolvedPhotoSettings,
                            bracketSettings: AVCaptureBracketedStillImageSettings?,
                            error: Error?) {
        if let error = error {
            delegate?.cameraViewDidFailToCaptureImage(error: error, cameraView: self)
            return
        }
        
        guard let photoSampleBuffer = photoSampleBuffer else { return }
        guard let jpegData = AVCapturePhotoOutput.jpegPhotoDataRepresentation(forJPEGSampleBuffer: photoSampleBuffer,
                                                                              previewPhotoSampleBuffer: nil) else { return }
        guard let jpegDataProvider = CGDataProvider(data: jpegData as CFData) else { return }
        guard let cgImage = CGImage(jpegDataProviderSource: jpegDataProvider,
                                    decode: nil,
                                    shouldInterpolate: true,
                                    intent: .absoluteColorimetric) else { return }
        
        let image = UIImage(cgImage: cgImage, scale: 1.0, orientation: .up)
        delegate?.cameraViewDidCaptureImage(image: image, cameraView: self)
    }
}
