import 'dart:convert';

LoginResponse loginResponseFromJson(String str) =>
    LoginResponse.fromJson(json.decode(str) as Map<String, dynamic>);

String loginResponseToJson(LoginResponse data) => json.encode(data.toJson());

class LoginResponse {
  LoginResponse({
    required this.result,
    this.error,
    required this.data,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) => LoginResponse(
        result: json['result'].toString(),
        error: json['error'],
        data: Data.fromJson(json['data'] as Map<String, dynamic>),
      );

  String result;
  dynamic error;
  Data data;

  Map<String, dynamic> toJson() => <String, dynamic>{
        'result': result,
        'error': error,
        'data': data.toJson(),
      };
}

class Data {
  Data({
    required this.sessionKey,
    required this.needMfaToken,
    required this.authappEnabled,
    required this.smsEnabled,
    required this.emailEnabled,
  });

  factory Data.fromJson(Map<String, dynamic> json) => Data(
        sessionKey: json['sessionKey'].toString(),
        needMfaToken: json['needMfaToken'] == true,
        authappEnabled: json['authapp_enabled'] as int,
        smsEnabled: json['sms_enabled'] as int,
        emailEnabled: json['email_enabled'] as int,
      );

  String sessionKey;
  bool needMfaToken;
  int authappEnabled;
  int smsEnabled;
  int emailEnabled;

  Map<String, dynamic> toJson() => <String, dynamic>{
        'sessionKey': sessionKey,
        'needMfaToken': needMfaToken,
        'authapp_enabled': authappEnabled,
        'sms_enabled': smsEnabled,
        'email_enabled': emailEnabled,
      };
}
