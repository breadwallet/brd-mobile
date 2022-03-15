import 'dart:developer';
import 'dart:io';

import 'package:camera/camera.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:kyc/common/app_theme.dart';
import 'package:kyc/kyc/view/kyc_page.dart';
import 'package:kyc/l10n/l10n.dart';
import 'package:kyc/utils/camera_manager.dart';
import 'package:kyc/widgets/error_snackbar.dart';

class PhotoTakenDetails {
  PhotoTakenDetails(this.photo);

  final XFile photo;
}

typedef PhotoTakenCallback = Future<void> Function(PhotoTakenDetails details);

class CameraPhotoTaker extends StatefulWidget {
  const CameraPhotoTaker(
      {required this.onTaken,
      this.preferredCameraDirection,
      this.resolutionPreset = ResolutionPreset.max,
      this.instructionsText});

  final PhotoTakenCallback onTaken;
  final CameraLensDirection? preferredCameraDirection;
  final ResolutionPreset resolutionPreset;
  final String? instructionsText;

  @override
  State<StatefulWidget> createState() {
    return CameraPhotoTakerState();
  }
}

class CameraPhotoTakerState extends State<CameraPhotoTaker>
    with WidgetsBindingObserver {
  CameraController? cameraController;
  XFile? photo;
  bool working = false;

  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance?.addObserver(this);

    if (CameraManager().hasCameras) {
      // If we want a specific camera, switch to it.
      if (widget.preferredCameraDirection != null) {
        CameraManager().switchToCamera(widget.preferredCameraDirection!);
      }
      // Start the camera
      onNewCameraSelected(CameraManager().camera!);
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance?.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    final camCon = cameraController;

    // App state changed before we got the chance to initialize.
    if (camCon == null || !camCon.value.isInitialized) {
      return;
    }

    if (state == AppLifecycleState.inactive) {
      camCon.dispose();
    } else if (state == AppLifecycleState.resumed) {
      onNewCameraSelected(camCon.description);
    }
  }

  void onNewCameraSelected(CameraDescription cameraDescription) async {
    if (cameraController != null) {
      await cameraController!.dispose();
    }

    final camCon = CameraController(
      cameraDescription,
      widget.resolutionPreset,
      enableAudio: false,
      imageFormatGroup: ImageFormatGroup.jpeg,
    );
    cameraController = camCon;

    // If the controller is updated then update the UI.
    camCon.addListener(() {
      if (mounted) setState(() {});
      if (camCon.value.hasError) {
        showInSnackBar('Camera error ${camCon.value.errorDescription}');
      }
    });

    try {
      await camCon.initialize();
    } on CameraException catch (e) {
      _showCameraException(e);
    }

    if (mounted) {
      setState(() {});
    }
  }

  void logError(String code, String? message) {
    if (message != null) {
      log('Error: $code\nError message: $message');
    } else {
      log('Error: $code');
    }
  }

  void showInSnackBar(String message) {
    ScaffoldMessenger.of(context)
      ..removeCurrentSnackBar()
      ..showSnackBar(ErrorSnackBar.fromString(message));
  }

  void _showCameraException(CameraException ex) {
    logError(ex.code, ex.description);
    showInSnackBar('Error: ${ex.code}\n${ex.description}');
  }

  Future<XFile?> takePicture() async {
    final camCon = cameraController;
    if (camCon == null || !camCon.value.isInitialized) {
      showInSnackBar('Select a camera first');
      return null;
    }

    if (camCon.value.isTakingPicture) {
      // A capture is already pending, do nothing.
      return null;
    }

    try {
      final file = await camCon.takePicture();
      return file;
    } on CameraException catch (ex) {
      _showCameraException(ex);
      return null;
    }
  }

  Future<void> onTakePictureButtonPressed() async {
    if (photo != null) {
      return;
    }

    if (kIsWeb && mounted) {
      setState(() {
        photo = XFile('');
      });
    }

    final file = await takePicture();

    if (file != null && mounted) {
      setState(() {
        photo = file;
      });
    }
  }

  void onSwitchCameraButtonPressed() {
    if (CameraManager().switchToNextCamera()) {
      onNewCameraSelected(CameraManager().camera!);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final cameraManager = CameraManager();

    return KycPageBody(
      child: LayoutBuilder(
        builder: (context, layoutConstraints) {
          // print(layoutConstraints);

          // Hide info text or make it smaller if there is not enough room
          var showInfoText = true;
          var infoTextStyle = theme.textTheme.bodyText2!;
          if (layoutConstraints.maxHeight < 250) {
            showInfoText = false;
          } else if (layoutConstraints.maxHeight < 350) {
            infoTextStyle = infoTextStyle.copyWith(fontSize: 10, height: 1);
          }

          return Column(
            children: [
              if (widget.instructionsText != null && showInfoText)
                Container(
                  margin: const EdgeInsets.only(bottom: 20),
                  child: Text(
                    widget.instructionsText!,
                    textAlign: TextAlign.center,
                    style: infoTextStyle,
                  ),
                ),
              Expanded(
                child: Container(
                  decoration: BoxDecoration(
                    color: lightGrey,
                    borderRadius: BorderRadius.circular(15),
                    border: Border.all(
                      color: const Color(0xffeeeeee),
                    ),
                  ),
                  child: Center(
                    child: photo == null
                        ? Stack(
                            alignment: Alignment.center,
                            children: [
                              _cameraPreviewWidget(),
                              Container(
                                alignment: Alignment.topRight,
                                child: IconButton(
                                  onPressed: cameraManager.canSwitchCamera
                                      ? onSwitchCameraButtonPressed
                                      : null,
                                  icon: const Icon(Icons.repeat),
                                ),
                              )
                            ],
                          )
                        : Container(
                            color: Colors.green.shade300,
                            child: Image.file(
                              File(photo!.path),
                            ),
                          ),
                  ),
                ),
              ),
              _buttonRowWidget(context),
            ],
          );
        },
      ),
    );
  }

  Widget _buttonRowWidget(BuildContext context) {
    final l10n = context.l10n;

    return Container(
      margin: const EdgeInsets.only(top: 12),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            child: OutlinedButton.icon(
              icon: const Icon(Icons.clear),
              label: Text(l10n.retryButtonText),
              onPressed: (photo != null && !working)
                  ? () async {
                      try {
                        await File(photo!.path).delete();
                      } catch (ex) {
                        print('Failed to delete temp photo: ${photo!.path}');
                      }
                      setState(() {
                        photo = null;
                      });
                    }
                  : null,
              clipBehavior: Clip.hardEdge,
              style: AppTheme.redOutlinedButtonStyle.copyWith(
                padding: MaterialStateProperty.all(EdgeInsets.zero),
              ),
            ),
          ),
          Container(
            width: 64,
            height: 64,
            margin: const EdgeInsets.symmetric(horizontal: 12),
            child: working
                ? Container(
                    padding: const EdgeInsets.all(16),
                    child: const CircularProgressIndicator(),
                  )
                : ElevatedButton(
                    onPressed: onTakePictureButtonPressed,
                    style: AppTheme.buttonStyleIntrinsicSize.copyWith(
                      backgroundColor: MaterialStateProperty.all(almostBlack),
                      foregroundColor: MaterialStateProperty.all(white),
                      overlayColor: MaterialStateProperty.all(grey1),
                      shape: MaterialStateProperty.all(const CircleBorder()),
                    ),
                    child: const Icon(
                      Icons.camera_alt_outlined,
                      size: 32,
                    ),
                  ),
          ),
          Expanded(
            child: OutlinedButton.icon(
              icon: const Icon(Icons.task_alt),
              label: Text(l10n.nextButtonText),
              onPressed: (photo != null && !working) ? onNextPressed : null,
              clipBehavior: Clip.hardEdge,
              style: AppTheme.greenOutlinedButtonStyle.copyWith(
                padding: MaterialStateProperty.all(EdgeInsets.zero),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> onNextPressed() async {
    if (photo == null) {
      return;
    }

    setState(() {
      working = true;
    });
    try {
      await widget.onTaken(PhotoTakenDetails(photo!));
    } finally {
      setState(() {
        working = false;
      });
    }
  }

  /// Display the preview from the camera
  /// (or a message if the preview is not available).
  Widget _cameraPreviewWidget() {
    final camCon = cameraController;
    final theme = Theme.of(context);

    if (camCon == null || !camCon.value.isInitialized) {
      return Text('No camera', style: theme.textTheme.headline6);
    } else {
      return CameraPreview(
        camCon,
        child: LayoutBuilder(
          builder: (context, constraints) {
            return GestureDetector(
              behavior: HitTestBehavior.opaque,
              onTapDown: (details) => onViewFinderTap(details, constraints),
            );
          },
        ),
      );
    }
  }

  void onViewFinderTap(TapDownDetails details, BoxConstraints constraints) {
    final camCon = cameraController;
    if (camCon == null) {
      return;
    }

    final offset = Offset(
      details.localPosition.dx / constraints.maxWidth,
      details.localPosition.dy / constraints.maxHeight,
    );
    camCon
      ..setExposurePoint(offset)
      ..setFocusPoint(offset);
  }
}
