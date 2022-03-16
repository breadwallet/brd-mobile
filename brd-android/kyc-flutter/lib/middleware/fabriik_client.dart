import 'package:dio/dio.dart';
import 'package:kyc/constants/constants.dart';
import 'package:kyc/kyc/models/kyc_status.dart';
import 'package:kyc/middleware/models/get_auth_user.dart';
import 'package:kyc/middleware/models/get_kyc_pi.dart';
import 'package:kyc/middleware/models/merapi.dart';
import 'package:kyc/middleware/models/post_kyc_pi.dart';
import 'package:kyc/middleware/models/post_kyc_upload.dart';
import 'package:kyc/models/login_response.dart';

class FabriikClient {
  final Dio _httpClient = Dio(BaseOptions(baseUrl: baseUrl))
    ..interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          print('REQUEST[${options.method}: ${options.path}]');
          return handler.next(options);
        },
      ),
    );

  Future<LoginResponse> login({
    required String email,
    required String password,
  }) async {
    final response = await _httpClient.post<Map<String, dynamic>>(
      'auth/login',
      data: {'username': email, 'password': password},
    );
    return LoginResponse.fromJson(response.data!);
  }

  Future<Response> register(String email, String password, String firstName,
      String lastName, String phone) async {
    try {
      final response = await _httpClient.post<dynamic>(
        'auth/register',
        data: {
          'first_name': firstName,
          'last_name': lastName,
          'email': email,
          'phone': phone,
          'encryptSHA512hex_password': password
        },
      );
      return response;
    } on DioError {
      rethrow;
    }
  }

  Future<Response> changePassword(
    String oldPassword,
    String newPassword,
    String sessionKey,
  ) async {
    try {
      final response = await _httpClient.post<dynamic>(
        'auth/changepsw',
        data: {
          'old_password': oldPassword,
          'new_password': newPassword,
          'sessionKey': sessionKey,
        },
      );
      return response;
    } on DioError {
      rethrow;
    }
  }

  Future<Map<String, dynamic>> _fetchJson(
      {required String path,
      required String method,
      MerapiInputData? data,
      bool sendSessionKey = true,
      String? sessionKey,
      SessionExpiredCallback? onExpired,
      bool useFormData = false}) async {
    final options = Options().compose(_httpClient.options, path)
      ..method = method;

    if (data != null) {
      final dataMap = await data.toMap();
      options.data = useFormData ? FormData.fromMap(dataMap) : dataMap;
    }

    // If we must send a session key
    if (sendSessionKey) {
      // Do we have a session key?
      if (sessionKey == null) {
        // If we found no session key, try to call
        // the onExpired handler, otherwise throw
        if (onExpired != null) {
          await onExpired();
          return _emptyMap;
        } else {
          throw MerapiSessionExpiredError();
        }
      } else {
        // We have a session key, send it in headers.
        options.headers['sessionKey'] = sessionKey;
      }
    }

    try {
      final response = await _httpClient.fetch<Map<String, dynamic>>(options);
      return response.data?['data'] as Map<String, dynamic>;
    } on DioError catch (ex) {
      // If we have information for a MerapiError
      if (ex.response?.data != null) {
        final dynamic jsonResponse = ex.response!.data;

        if (jsonResponse != null) {
          // Create MerapiError instance
          final error =
              MerapiError.fromJson(jsonResponse as Map<String, dynamic>);

          // Try to handle session errors here
          switch (error.code.toString()) {
            case MerapiErrorCode.sessionTimeout:
            case MerapiErrorCode.accountLocked:
            case MerapiErrorCode.accountSuspended:
              if (onExpired != null) {
                await onExpired();
                return _emptyMap;
              }

              throw MerapiSessionExpiredError();
          }

          // Or if we can't handle them, throw the MerapiError
          throw error;
        }
      }

      // Throw other error
      rethrow;
    }
  }

  Future<void> setKycPi(PostKycPiRequest data, SessionInfo si) async {
    await _fetchJson(
      sessionKey: si.sessionKey,
      onExpired: si.onExpired,
      path: 'kyc/pi',
      method: 'POST',
      data: data,
    );
  }

  Future<GetAuthUserResponse> getAuthUser(SessionInfo si) async {
    final json = await _fetchJson(
      sessionKey: si.sessionKey,
      onExpired: si.onExpired,
      path: 'auth/user',
      method: 'GET',
    );
    return GetAuthUserResponse.fromJson(json);
  }

  Future<void> setKycDocument(PostKycUploadRequest data, SessionInfo si) async {
    await _fetchJson(
      sessionKey: si.sessionKey,
      onExpired: si.onExpired,
      path: 'kyc/upload',
      method: 'POST',
      useFormData: true,
      data: data,
    );
  }

  Future<GetKycPiResponse> getKycPi(SessionInfo si) async {
    final json = await _fetchJson(
      sessionKey: si.sessionKey,
      path: 'kyc/pi',
      method: 'GET',
    );
    return GetKycPiResponse.fromJson(json['items'][0] as Map<String, dynamic>);
  }

  Future<KycStatus> getKycStatus(SessionInfo si) async {
    final json = await _fetchJson(
      sessionKey: si.sessionKey,
      path: 'kyc/status',
      method: 'GET',
    );
    return KycStatus.fromJson(json['items'][0] as Map<String, dynamic>);
  }
}

typedef SessionExpiredCallback = Future<void> Function();

const _emptyMap = <String, dynamic>{};

class SessionInfo {
  SessionInfo({required this.sessionKey, this.onExpired});

  final String? sessionKey;
  final SessionExpiredCallback? onExpired;
}
