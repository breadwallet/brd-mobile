import 'package:kyc/models/user.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SessionManager {
  static const String prefKeyCurrentUserFirstName =
      'PREF_KEY_CURRENT_USER_FIRST_NAME';
  static const String prefKeyCurrentUserLastName =
      'PREF_KEY_CURRENT_USER_LAST_NAME';
  static const String prefKeyCurrentUserEmail = 'PREF_KEY_CURRENT_USER_EMAIL';
  static const String prefKeyCurrentUserPhone = 'PREF_KEY_CURRENT_USER_PHONE';
  static const String prefKeyCurrentUserSessionKey =
      'PREF_KEY_CURRENT_USER_SESSION_KEY';
  static const String prefKeyBiometricsStatus = 'PREF_KEY_BIOMETRICS_STATUS';
  static const String prefKeyGlobalCurrency = 'PREF_KEY_GLOBAL_CURRENCY';

  late SharedPreferences mPrefs;

  void persistSessionKey(String sessionKey) async {
    await getPrefInstance();
    await mPrefs.setString(prefKeyCurrentUserSessionKey, sessionKey);
  }

  Future<String?> getSessionKey() async {
    await getPrefInstance();
    return mPrefs.getString(prefKeyCurrentUserSessionKey);
  }

  Future getPrefInstance() async {
    mPrefs = await SharedPreferences.getInstance();
  }

  Future<String?> getUserFirstName() async {
    await getPrefInstance();
    return mPrefs.getString(prefKeyCurrentUserFirstName);
  }

  void setUserFirstName(String firstName) async {
    await getPrefInstance();
    await mPrefs.setString(prefKeyCurrentUserFirstName, firstName);
  }

  void setGlobalCurrency(String currency) async {
    await getPrefInstance();
    await mPrefs.setString(prefKeyGlobalCurrency, currency);
  }

  Future<String?> getGlobalCurrency() async {
    await getPrefInstance();
    return mPrefs.getString(prefKeyGlobalCurrency);
  }

  Future<bool?> getBiometricsStatus() async {
    await getPrefInstance();
    return mPrefs.getBool(prefKeyBiometricsStatus);
  }

  void setBiometricsStatus(bool isBiometricsOn) async {
    await getPrefInstance();
    await mPrefs.setBool(prefKeyBiometricsStatus, isBiometricsOn);
  }

  Future<String?> getUserLastName() async {
    await getPrefInstance();
    return mPrefs.getString(prefKeyCurrentUserLastName);
  }

  void setUserLastName(String lastName) async {
    await getPrefInstance();
    await mPrefs.setString(prefKeyCurrentUserLastName, lastName);
  }

  Future<String?> getUserEmail() async {
    await getPrefInstance();
    return mPrefs.getString(prefKeyCurrentUserEmail);
  }

  void setUserEmail(String email) async {
    await getPrefInstance();
    await mPrefs.setString(prefKeyCurrentUserEmail, email);
  }

  Future<String?> getUserPhone() async {
    await getPrefInstance();
    return mPrefs.getString(prefKeyCurrentUserPhone);
  }

  void setUserPhone(String phone) async {
    await getPrefInstance();
    await mPrefs.setString(prefKeyCurrentUserPhone, phone);
  }

  Future<User?> getUserDetails() async {
    final userFirstName = await getUserFirstName();
    final userLastName = await getUserLastName();
    final userEmail = await getUserEmail();
    final userPhone = await getUserPhone();
    if (userFirstName != null &&
        userLastName != null &&
        userEmail != null &&
        userPhone != null) {
      return User(userFirstName, userLastName, userEmail, userPhone);
    } else {
      return null;
    }
  }

  void setUserData(User user) {
    setUserFirstName(user.firstName);
    setUserLastName(user.lastName);
    setUserEmail(user.email);
    setUserPhone(user.phone);
  }

  Future<void> clearPrefData() async {
    await mPrefs.clear();
  }
}
