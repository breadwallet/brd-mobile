import 'package:equatable/equatable.dart';
import 'package:kyc/middleware/models/get_kyc_pi.dart';
import 'package:kyc/middleware/models/post_kyc_pi.dart';

class KycPi extends Equatable {
  const KycPi(
      {this.countryCode,
      this.stateCode,
      this.street,
      this.city,
      this.zipCode,
      this.dateOfBirth,
      this.taxIdNumber});

  KycPi.fromApi(GetKycPiResponse response)
      : countryCode = response.countryCode,
        stateCode = response.stateCode,
        street = response.street,
        city = response.city,
        zipCode = response.zipCode,
        dateOfBirth = response.dateOfBirth,
        taxIdNumber = response.taxIdNumber;

  /// Two-letter country code
  final String? countryCode;

  /// Two-letter state code
  final String? stateCode;

  /// Street address, including number and detail.
  final String? street;

  /// City name
  final String? city;

  /// ZIP code
  final String? zipCode;

  /// Date of birth
  final DateTime? dateOfBirth;

  /// 9-digit tax ID number
  final String? taxIdNumber;

  @override
  List<Object?> get props =>
      [countryCode, stateCode, street, city, zipCode, dateOfBirth, taxIdNumber];

  @override
  bool? get stringify => true;

  bool isValid() {
    return (countryCode ?? '').isNotEmpty &&
        (!countryRequiresState(false) || (stateCode ?? '').isNotEmpty) &&
        (street ?? '').isNotEmpty &&
        (city ?? '').isNotEmpty &&
        (zipCode ?? '').isNotEmpty &&
        (dateOfBirth != null) &&
        (taxIdNumber ?? '').isNotEmpty;
  }

  PostKycPiRequest toApi() {
    if (!isValid()) {
      throw Exception('Invalid data');
    }

    return PostKycPiRequest(
      countryCode: countryCode!,
      stateCode: stateCode ?? 'N/A',
      street: street!,
      city: city!,
      zipCode: zipCode!,
      dateOfBirth: dateOfBirth!,
      taxIdNumber: taxIdNumber!,
    );
  }

  KycPi copyWith(
      {String? countryCode,
      String? stateCode,
      String? street,
      String? city,
      String? zipCode,
      DateTime? dateOfBirth,
      String? taxIdNumber}) {
    return KycPi(
      countryCode: countryCode ?? this.countryCode,
      stateCode: stateCode ?? this.stateCode,
      street: street ?? this.street,
      city: city ?? this.city,
      zipCode: zipCode ?? this.zipCode,
      dateOfBirth: dateOfBirth ?? this.dateOfBirth,
      taxIdNumber: taxIdNumber ?? this.taxIdNumber,
    );
  }

  bool countryRequiresState(bool defaultRequires) {
    return countryCode == null ? defaultRequires : countryCode == 'US';
  }
}

class KycPiFieldErrors extends Equatable {
  KycPiFieldErrors() : errors = {};

  KycPiFieldErrors.fromApi(PostKycPiErrorData data) : errors = data.parameters;

  final Map<String, String?> errors;

  @override
  List<Object?> get props => [errors];

  @override
  bool? get stringify => true;

  String? getError(String fieldName) => errors[fieldName];
}
