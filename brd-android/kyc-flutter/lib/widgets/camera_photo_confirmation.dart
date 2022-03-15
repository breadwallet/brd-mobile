import 'dart:typed_data';
import 'package:camera/camera.dart';
import 'package:flutter/material.dart';

typedef AcceptCallback = Future<void> Function();
typedef RetryCallback = Future<void> Function();

class CameraPhotoConfirmation extends StatefulWidget {
  const CameraPhotoConfirmation(
      {required this.photo,
      required this.onAccept,
      required this.onRetry,
      this.headlineText,
      this.instructionsText});

  final XFile? photo;
  final AcceptCallback onAccept;
  final RetryCallback onRetry;
  final String? headlineText;
  final String? instructionsText;

  @override
  State<StatefulWidget> createState() {
    return CameraPhotoConfirmationState();
  }
}

class CameraPhotoConfirmationState extends State<CameraPhotoConfirmation> {
  Uint8List? photoBytes;
  bool working = false;

  @override
  void initState() {
    super.initState();

    if (widget.photo != null) {
      widget.photo!.readAsBytes().then((value) {
        if (mounted) {
          setState(() {
            photoBytes = value;
          });
        }
      });
    }
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 300),
        child: Column(
          children: [
            Container(
              margin: const EdgeInsets.fromLTRB(32, 32, 32, 32),
              width: MediaQuery.of(context).size.width,
              child: Image.asset('assets/nakamoto.png'),
            ),
            if (widget.headlineText != null)
              Container(
                margin: const EdgeInsets.fromLTRB(32, 0, 32, 16),
                child: Center(
                  child: Text(
                    widget.headlineText!,
                    style: theme.textTheme.headline6,
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            Expanded(child: Container(child: Center(child: _imagePreview()))),
            if (widget.instructionsText != null)
              Container(
                margin: const EdgeInsets.only(top: 16, bottom: 16),
                child: Text(
                  widget.instructionsText!,
                  textAlign: TextAlign.center,
                ),
              ),
            if (working)
              Container(
                margin: const EdgeInsets.fromLTRB(0, 0, 0, 16),
                child: ConstrainedBox(
                  constraints: const BoxConstraints(minHeight: 48),
                  child: const Center(child: CircularProgressIndicator()),
                ),
              )
            else
              Container(
                margin: const EdgeInsets.fromLTRB(0, 0, 0, 16),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: ConstrainedBox(
                        constraints: BoxConstraints.tightFor(
                          width: MediaQuery.of(context).size.width,
                          height: 48,
                        ),
                        child: ElevatedButton(
                          onPressed: onRetryPressed,
                          child: const Text('Retry'),
                        ),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: ConstrainedBox(
                        constraints: BoxConstraints.tightFor(
                          width: MediaQuery.of(context).size.width,
                          height: 48,
                        ),
                        child: ElevatedButton(
                          onPressed:
                              photoBytes != null ? onAcceptPressed : null,
                          child: const Text('Next'),
                        ),
                      ),
                    )
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }

  Future<void> onRetryPressed() async {
    await widget.onRetry();
  }

  Future<void> onAcceptPressed() async {
    setState(() {
      working = true;
    });
    try {
      await widget.onAccept();
    } finally {
      setState(() {
        working = false;
      });
    }
  }

  Widget _imagePreview() {
    if (photoBytes != null) {
      return Image.memory(photoBytes!);
    }
    return const Placeholder();
  }
}
