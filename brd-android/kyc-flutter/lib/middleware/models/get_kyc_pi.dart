import 'merapi.dart';

class GetKycPiResponse {
  GetKycPiResponse(
      {this.countryCode,
      this.stateCode,
      this.street,
      this.city,
      this.zipCode,
      this.dateOfBirth,
      this.taxIdNumber});

  GetKycPiResponse.fromJson(Map<String, dynamic> json)
      : countryCode = json['country'] as String?,
        stateCode = json['state'] as String?,
        street = json['street'] as String?,
        city = json['city'] as String?,
        zipCode = json['zip'] as String?,
        dateOfBirth =
            MerapiJson.parseMerapiDate(json['date_of_birth'] as String?),
        taxIdNumber = json['tax_id_number'] as String?;

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
}
