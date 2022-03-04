// 
// Created by Equaleyes Solutions Ltd
//

import UIKit

protocol KYCUploadBusinessLogic {
    // MARK: Business logic functions
    
    func saveImage(request: KYCUpload.SaveImages.Request)
}

protocol KYCUploadDataStore {
    // MARK: Data store
    
}

class KYCUploadInteractor: KYCUploadBusinessLogic, KYCUploadDataStore {
    var presenter: KYCUploadPresentationLogic?
    
    // MARK: Interactor functions
    
    func saveImage(request: KYCUpload.SaveImages.Request) {
        switch request.type {
        case .selfie:
            guard let image = request.images.first?.jpegData(compressionQuality: 1.0) else { return }
            uploadSelfie(image: image)
        case .frontAndBack:
            guard let imageBack = request.images[1].jpegData(compressionQuality: 1.0),
                  let imageFront = request.images[0].jpegData(compressionQuality: 1.0) else { return }
            uploadFrontBack(images: [imageBack, imageFront])
        }
    }
    
    private func uploadSelfie(image: Data) {
        let worker = KYCUploadSelfieWorker()
        let workerUrlModelData = KYCUploadSelfieWorkerUrlModelData()
        let workerRequest = KYCUploadSelfieWorkerRequest(imageData: image)
        let workerData = KYCUploadSelfieWorkerData(workerRequest: workerRequest,
                                                  workerUrlModelData: workerUrlModelData)
        
        worker.execute(requestData: workerData) { [weak self] error in
            guard error == nil else {
                self?.presenter?.presentError(response: .init(error: error))
                return
            }
            
            self?.presenter?.presentSaveImage(response: .init())
        }
    }
    
    private func uploadFrontBack(images: [Data]) {
        let worker = KYCUploadFrontBackWorker()
        let workerUrlModelData = KYCUploadFrontBackWorkerUrlModelData()
        let workerRequest = KYCUploadFrontBackWorkerRequest(imageData: images)
        let workerData = KYCUploadFrontBackWorkerData(workerRequest: workerRequest,
                                                      workerUrlModelData: workerUrlModelData)
        
        worker.execute(requestData: workerData) { [weak self] error in
            guard error == nil else {
                self?.presenter?.presentError(response: .init(error: error))
                return
            }
            
            self?.presenter?.presentSaveImage(response: .init())
        }
    }
}
