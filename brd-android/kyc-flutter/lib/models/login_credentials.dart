class LoginCredentials {
  LoginCredentials({
    this.email,
    this.password,
  });

  factory LoginCredentials.fromJson(Map<String, dynamic> json) {
    return LoginCredentials(
      email: json['email'] as String?,
      password: json['password'] as String?,
    );
  }

  String? email;
  String? password;

  @override
  String toString() {
    return 'LoginDto{email: $email, password: $password}';
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'email': email,
        'password': password,
      };
}
