// class EmptyMerapiResponse {
//   EmptyMerapiResponse(this.result, this.error);
//   EmptyMerapiResponse.fromJson(Map<String, dynamic> json)
//       : result = json['result'] ?? '',
//         error = MerapiError.fromJson(json);

//   final String result;
//   final MerapiError? error;

//   bool get success {
//     return result == 'ok';
//   }
// }

// class MerapiResponse<T> extends EmptyMerapiResponse {
//   MerapiResponse(
//       {required String result,
//       MerapiError? error,
//       required this.json,
//       this.data})
//       : super(result, error);
//   MerapiResponse.fromJson(this.json) : super.fromJson(json);

//   Map<String, dynamic> json;
//   T? data;
// }

class MerapiError implements Exception {
  MerapiError({this.code, this.message, this.data});

  MerapiError.fromJson(Map<String, dynamic> json)
      : this(
          code: json['error']?['code'] as String?,
          message: json['error']?['server_message'] as String?,
          data: json['data'] as Map<String, dynamic>?,
        );

  /// Error code is usually a 3 digit code.
  final String? code;

  /// An error message that is not meant to be shown
  /// to the end user as-is.
  final String? message;

  /// Extra data related to the error
  final Map<String, dynamic>? data;

  @override
  String toString() {
    return '$code, $message';
  }
}

class MerapiSessionExpiredError extends MerapiError {
  MerapiSessionExpiredError(
      {String code = MerapiErrorCode.sessionTimeout,
      String message = 'Session timeout'})
      : super(code: code, message: message);
}

extension MerapiDateTime on DateTime {
  String toMerapiDate() {
    return '${year.toString()}-'
        '${month.toString().padLeft(2, "0")}-'
        '${day.toString().padLeft(2, "0")}';
  }
}

abstract class MerapiJson {
  static DateTime? parseMerapiDate(String? value) {
    if (value == null) {
      return null;
    }

    return DateTime.tryParse(value);
  }
}

abstract class MerapiInputData {
  Future<Map<String, dynamic>> toMap();
}

abstract class MerapiErrorCode {
  static const wrongPasswordResetCode = '11';
  static const wrongParameters = '103';
  static const sessionTimeout = '105';
  static const wrongLogin = '106';
  static const wrongCredentials = '107';
  static const accountLocked = '109';
  static const accountSuspended = '110';
  static const wrongKycData = '401';
}
