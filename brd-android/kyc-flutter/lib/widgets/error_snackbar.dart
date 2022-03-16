import 'package:flutter/material.dart';
import 'package:kyc/common/app_theme.dart';
import 'package:kyc/l10n/l10n.dart';
import 'package:kyc/middleware/models/merapi.dart';

class ErrorSnackBar extends SnackBar {
  ErrorSnackBar(String message)
      : super(
          content: Text(
            message,
            style: const TextStyle(color: white),
          ),
          backgroundColor: red,
          duration: const Duration(seconds: 8),
        );

  ErrorSnackBar.fromString(String? message)
      : this(message != null ? 'Error: $message' : 'Something went wrong');

  ErrorSnackBar.fromMerapiError(MerapiError error)
      : this.fromString(error.message);

  ErrorSnackBar.fromException(Exception error) : this(error.toString());

  ErrorSnackBar.fromLocaleMerapiError(MerapiError error, AppLocalizations l10n)
      : this(tryGetLocalizedMerapiErrorMessage(l10n, error));

  static String tryGetLocalizedMerapiErrorMessage(
      AppLocalizations l10n, MerapiError error) {
    if (error.code == MerapiErrorCode.wrongKycData) {
      return l10n.kycPiErrorGeneric;
    }

    // Fall back to the Merapi message
    if (error.message != null) {
      return l10n.errorAndMessage(error.message!);
    } else {
      return l10n.genericApiErrorMessage;
    }
  }
}
