import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:kyc/kyc/cubit/kyc_cubit.dart';
import 'package:kyc/kyc/models/kyc_doc_type.dart';
import 'package:kyc/kyc/view/kyc_page.dart';
import 'package:kyc/l10n/l10n.dart';
import 'package:kyc/utils/camera_manager.dart';
import 'package:kyc/widgets/error_snackbar.dart';

class KycSelectDocumentTypePage extends StatefulWidget {
  @override
  _KycSelectDocumentTypePage createState() => _KycSelectDocumentTypePage();
}

class _KycSelectDocumentTypePage extends State<KycSelectDocumentTypePage> {
  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    return KycPageScrollColumn(
      children: [
        Container(
          child: Text(l10n.kycSelectDocTypeHelpText),
        ),
        Container(
          margin: const EdgeInsets.only(top: 16),
          child: Column(
            children: [
              DocTypeButton(
                docType: KycDocType.driversLicense,
                onPressed: _onSubmit,
                label: l10n.kycDocTypeDriversLicense,
                icon: Icons.directions_car_outlined,
              ),
              DocTypeButton(
                docType: KycDocType.identityCard,
                onPressed: _onSubmit,
                label: l10n.kycDocTypeIdentityCard,
                icon: Icons.badge_outlined,
              ),
              DocTypeButton(
                docType: KycDocType.passport,
                onPressed: _onSubmit,
                label: l10n.kycDocTypePassport,
                icon: Icons.language_outlined,
              ),
              DocTypeButton(
                docType: KycDocType.residencePermit,
                onPressed: _onSubmit,
                label: l10n.kycDocTypeResidencePermit,
                icon: Icons.location_city_outlined,
              )
            ],
          ),
        ),
      ],
    );
  }

  Future<void> _onSubmit(KycDocType docType) async {
    context.read<KycCubit>().setDocType(docType);

    // Ensure we have cameras before moving to the next screens
    final cameraManager = CameraManager();

    if (kIsWeb) {
      await Navigator.of(context).pushNamed(routeKycScanFront);
      return;
    }

    if (await cameraManager.initialize()) {
      await Navigator.of(context).pushNamed(routeKycScanFront);
    } else if (cameraManager.lastError != null) {
      final error = cameraManager.lastError!;
      ScaffoldMessenger.of(context)
        ..clearSnackBars()
        ..showSnackBar(ErrorSnackBar.fromException(error));
      log('Error: ${error.code}\nError message: ${error.description}');
    } else {
      ScaffoldMessenger.of(context)
        ..clearSnackBars()
        ..showSnackBar(ErrorSnackBar('No available cameras!'));
      log('No available cameras!');
    }
  }
}

typedef OnPressedHandler = void Function(KycDocType docType);

class DocTypeButton extends StatefulWidget {
  const DocTypeButton({
    required this.docType,
    required this.onPressed,
    required this.label,
    required this.icon,
  });

  final KycDocType docType;
  final OnPressedHandler onPressed;
  final String label;
  final IconData icon;

  @override
  _DocTypeButton createState() => _DocTypeButton();
}

class _DocTypeButton extends State<DocTypeButton> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: OutlinedButton(
        onPressed: onPressed,
        child: Row(
          children: [
            Icon(
              widget.icon,
              color: theme.accentIconTheme.color,
            ),
            const SizedBox(width: 16),
            Text(widget.label),
            Expanded(child: Container()),
            Icon(
              Icons.keyboard_arrow_right,
              color: theme.accentIconTheme.color,
            ),
          ],
        ),
      ),
    );
  }

  void onPressed() {
    widget.onPressed(widget.docType);
  }
}
