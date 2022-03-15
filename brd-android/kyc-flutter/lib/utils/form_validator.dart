class FormValidator {
  static String? formValidation(String? value) {
    return (value ?? '').isEmpty ? 'Cannot be empty' : null;
  }

  static String? confirmPasswordValidation(
      String? password, String? confirmPassword) {
    return (password != confirmPassword) ? 'Password does not match' : null;
  }

  static String? nonNullOrEmptyValidator(String? value) {
    return (value ?? '').isEmpty ? 'Cannot be empty' : null;
  }

  static String? nonNullValidator<T>(T? value) {
    return value == null ? 'Cannot be empty' : null;
  }

  static String? emailAddressValidator(String? val) {
    if (isFalsy(val!)) {
      return 'Invalid email';
    }
    val = val.trim();
    if (!validateEmail(val)) {
      return 'Invalid email';
    }
    return null;
  }

  static bool isFalsy(String val) => val.trim().isEmpty;

  static bool validateEmail(String value) {
    const pattern =
        r'^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$';
    final regex = RegExp(pattern);
    return (!regex.hasMatch(value)) ? false : true;
  }
}
