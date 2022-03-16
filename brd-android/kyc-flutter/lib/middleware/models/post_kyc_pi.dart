import 'package:equatable/equatable.dart';
import 'package:kyc/middleware/models/merapi.dart';

class PostKycPiRequest extends Equatable implements MerapiInputData {
  const PostKycPiRequest(
      {required this.countryCode,
      required this.stateCode,
      required this.street,
      required this.city,
      required this.zipCode,
      required this.dateOfBirth,
      required this.taxIdNumber});

  /// Two-letter country code
  final String countryCode;

  /// Two-letter state code
  final String stateCode;

  /// Street address, including number and detail.
  final String street;

  /// City name
  final String city;

  /// ZIP code
  final String zipCode;

  /// Date of birth
  final DateTime dateOfBirth;

  /// 9-digit tax ID number
  final String taxIdNumber;

  @override
  Future<Map<String, dynamic>> toMap() async {
    return <String, dynamic>{
      countryCodeParamName: countryCode,
      stateParamName: stateCode,
      streetParamName: street,
      cityParamName: city,
      zipParamName: zipCode,
      dateOfBirthParamName: dateOfBirth.toMerapiDate(),
      taxIdNumberParamName: taxIdNumber
    };
  }

  @override
  List<Object> get props =>
      [countryCode, stateCode, street, city, zipCode, dateOfBirth, taxIdNumber];

  PostKycPiRequest copyWith(
      {String? countryCode,
      String? stateCode,
      String? street,
      String? city,
      String? zipCode,
      DateTime? dateOfBirth,
      String? taxIdNumber}) {
    return PostKycPiRequest(
      countryCode: countryCode ?? this.countryCode,
      stateCode: stateCode ?? this.stateCode,
      street: street ?? this.street,
      city: city ?? this.city,
      zipCode: zipCode ?? this.zipCode,
      dateOfBirth: dateOfBirth ?? this.dateOfBirth,
      taxIdNumber: taxIdNumber ?? this.taxIdNumber,
    );
  }

  static const countryCodeParamName = 'country';
  static const stateParamName = 'state';
  static const streetParamName = 'street';
  static const cityParamName = 'city';
  static const zipParamName = 'zip';
  static const dateOfBirthParamName = 'date_of_birth';
  static const taxIdNumberParamName = 'tax_id_number';
}

class PostKycPiErrorData {
  PostKycPiErrorData.fromJson(Map<String, dynamic> data)
      : parameters = _parseData(data);

  PostKycPiErrorData.fromMerapiError(MerapiError error)
      : this.fromJson(error.data!);

  /// Parameters that contain errors
  final Map<String, String?> parameters;

  static Map<String, String?> _parseData(Map<String, dynamic> data) {
    final parameters = <String, String?>{};

    if (data['items'] != null) {
      final items = data['items'] as List<Map<String, dynamic>>;
      for (final item in items) {
        if (item['parameter'] != null) {
          parameters[item['parameter'].toString()] =
              item['exception'].toString();
        }
      }
    }

    return parameters;
  }
}
