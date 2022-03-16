import 'dart:io';

import 'package:dio/dio.dart';
import 'package:kyc/kyc/models/kyc_doc_type.dart';
import 'package:kyc/kyc/models/kyc_pi.dart';
import 'package:kyc/kyc/models/kyc_status.dart';
import 'package:kyc/middleware/fabriik_client.dart';
import 'package:kyc/middleware/models/merapi.dart';
import 'package:kyc/middleware/models/post_kyc_upload.dart';
import 'package:kyc/models/login_credentials.dart';
import 'package:kyc/models/user.dart';
import 'package:kyc/utils/session_manager.dart';

class UserRepo {
  UserRepo({this.onSessionExpired});

  final FabriikClient _fabriikClient = FabriikClient();
  final SessionManager _sessionManager = SessionManager();
  final SessionExpiredCallback? onSessionExpired;

  Future<bool> loginWithEmailPass(LoginCredentials loginCredentials) async {
    try {
      final result = await _fabriikClient.login(
        email: loginCredentials.email!.trim(),
        password: loginCredentials.password!,
      );
      if (result.result == 'ok') {
        _sessionManager
          ..setUserEmail(loginCredentials.email!.trim())
          ..persistSessionKey(result.data.sessionKey);
        return true;
      } else {
        throw Exception(result.error);
      }
    } on DioError catch (e) {
      if (e.response!.statusCode == 500) {
        final msg = e.response!.data['error']['server_message'].toString();
        throw HttpException(msg);
      } else {
        throw Exception(e);
      }
    } catch(e) {
      return false;
    }
  }

  void loginWithSessionKey(String sessionKey) {
    _sessionManager.persistSessionKey(sessionKey);
  }

  Future<User?> signUpWithEmail(String email, String password, String firstName,
      String lastName, String phone) async {
    try {
      final result = await _fabriikClient.register(
        email,
        password,
        firstName,
        lastName,
        phone,
      );
      if (result.data['result'] == 'ok') {
        final user = User(firstName, lastName, email, phone);
        _sessionManager
          ..setUserData(user)
          ..persistSessionKey(result.data['data']['sessionKey'].toString());
        return user;
      } else {
        throw result.data['error'].toString();
      }
    } on DioError catch (e) {
      if (e.response!.statusCode == 500) {
        throw HttpException(
          e.response!.data['error']['server_message'].toString(),
        );
      } else {
        throw Exception(e);
      }
    }
  }

  Future<void> logout() async {
    await _sessionManager.clearPrefData();
  }

  Future<bool> isLoggedIn() async {
    final session = await _sessionManager.getSessionKey();
    return session != null;
  }

  Future<User?> getCurrentUser() async {
    return await _sessionManager.getUserDetails();
  }

  Future<bool> changePassword(String oldPassword, String newPassword) async {
    try {
      final si = await _getSessionInfo();
      final result = await _fabriikClient.changePassword(
        oldPassword,
        newPassword,
        si.sessionKey!,
      );
      if (result.data['result'] == 'ok') {
        return true;
      } else {
        throw result.data['error'].toString();
      }
    } on DioError catch (e) {
      print(e);
      if (e.response!.statusCode == 500) {
        throw HttpException(
          e.response!.data['error']['server_message'].toString(),
        );
      } else {
        throw Exception(e);
      }
    }
  }

  Future<SessionInfo> _getSessionInfo([bool setOnExpiredHandler = true]) async {
    final sessionKey = await _sessionManager.getSessionKey();
    return SessionInfo(
      sessionKey: sessionKey,
      onExpired: setOnExpiredHandler ? onSessionExpired : null,
    );
  }

  Future<void> setKycPi(KycPi pi) async {
    final data = pi.toApi();
    final si = await _getSessionInfo();
    await _fabriikClient.setKycPi(data, si);
  }

  Future<KycPi> getKycPi() async {
    final si = await _getSessionInfo();
    final response = await _fabriikClient.getKycPi(si);
    return KycPi.fromApi(response);
  }

  Future<void> setKycDocument(
      KycDocType docType, KycDocSide side, String filePath) async {
    final data = PostKycUploadRequest(
      docType: docType.toMerapiDocType(side),
      filePath: filePath,
    );
    final si = await _getSessionInfo();
    await _fabriikClient.setKycDocument(data, si);
  }

  Future<KycStatus> getKycStatus() async {
    final si = await _getSessionInfo();
    return await _fabriikClient.getKycStatus(si);
  }

  Future<bool> validateSession() async {
    // We want this to throw if the session is invalid,
    // not to navigate to the sign-in page.
    final si = await _getSessionInfo(false);
    try {
      await _fabriikClient.getAuthUser(si);
    } on MerapiSessionExpiredError {
      return false;
    }

    return true;
  }
}
