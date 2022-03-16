import 'package:camera/camera.dart';
import 'package:flutter/cupertino.dart';

class CameraManager {
  CameraManager._privateConstructor();

  factory CameraManager() {
    return _instance;
  }

  static final CameraManager _instance = CameraManager._privateConstructor();

  List<CameraDescription> _cameras = [];
  int _selectedCameraIndex = 0;
  CameraException? _lastException;

  Future<bool> initialize() async {
    var cameras = _cameras;

    if (cameras.isEmpty) {
      try {
        WidgetsFlutterBinding.ensureInitialized();

        cameras = await availableCameras();
        if (cameras.isNotEmpty) {
          _cameras = cameras;
          _selectedCameraIndex = 0;
          return true;
        } else {
          return false;
        }
      } on CameraException catch (ex) {
        _lastException = ex;
        // TODO: log
        print('Error: ${ex.code}\nMessage: ${ex.description}');
        return false;
      }
    } else {
      return cameras.isNotEmpty;
    }
  }

  bool get hasCameras {
    return _cameras.isNotEmpty;
  }

  CameraException? get lastError {
    return _lastException;
  }

  CameraDescription? get camera {
    if (_selectedCameraIndex >= _cameras.length) {
      _selectedCameraIndex = 0;
    }

    if (_selectedCameraIndex < _cameras.length) {
      return _cameras[_selectedCameraIndex];
    }
    return null;
  }

  bool get canSwitchCamera {
    return _cameras.length >= 2;
  }

  bool switchToNextCamera() {
    final currentIndex = _selectedCameraIndex;

    var newIndex = currentIndex + 1;
    if (newIndex >= _cameras.length) {
      newIndex = 0;
    }

    if (newIndex != currentIndex) {
      _selectedCameraIndex = newIndex;
      return true;
    }

    return false;
  }

  bool switchToCamera(CameraLensDirection direction) {
    final newIndex = _cameras.indexWhere((cd) => cd.lensDirection == direction);
    if (newIndex != -1) {
      _selectedCameraIndex = newIndex;
      return true;
    }

    return false;
  }
}
