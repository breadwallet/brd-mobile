import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:kyc/common/dependency_provider.dart';
import 'package:kyc/kyc/cubit/kyc_cubit.dart';
import 'package:kyc/kyc/models/kyc_doc_type.dart';
import 'package:kyc/kyc/view/kyc_page.dart';
import 'package:kyc/l10n/l10n.dart';
import 'package:kyc/middleware/models/merapi.dart';
import 'package:kyc/widgets/camera_photo_taker.dart';
import 'package:kyc/widgets/error_snackbar.dart';

class KycScanFrontPage extends StatefulWidget {
  @override
  _KycScanFrontPageState createState() => _KycScanFrontPageState();
}

class _KycScanFrontPageState extends State<KycScanFrontPage> {
  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    return CameraPhotoTaker(
      onTaken: onTaken,
      preferredCameraDirection: CameraLensDirection.back,
      instructionsText: l10n.kycScanFront,
    );
  }

  Future<void> onTaken(PhotoTakenDetails details) async {
    final docType = context.read<KycCubit>().state.docType;

    final photoPath = details.photo.path;

    try {
      await DependencyProvider.of(context)
          .userRepo
          .setKycDocument(docType, KycDocSide.front, photoPath);

      await Navigator.of(context).pushNamed(routeKycScanBack);
    } on MerapiError catch (error) {
      ScaffoldMessenger.of(context)
        ..removeCurrentSnackBar()
        ..showSnackBar(
          ErrorSnackBar.fromLocaleMerapiError(error, context.l10n),
        );
    } on Exception catch (error) {
      ScaffoldMessenger.of(context)
        ..removeCurrentSnackBar()
        ..showSnackBar(ErrorSnackBar.fromException(error));
    }
  }
}
