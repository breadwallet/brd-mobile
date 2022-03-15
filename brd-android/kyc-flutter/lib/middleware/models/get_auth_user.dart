class GetAuthUserResponse {
  GetAuthUserResponse(
      {required this.id,
      required this.username,
      required this.email,
      required this.roles});

  GetAuthUserResponse.fromJson(Map<String, dynamic> json)
      : id = json['id'] as int,
        username = json['username'] as String,
        email = json['email'] as String,
        roles = Set<String>.from(json['roles'] as Iterable<String>);

  /// User id
  final int id;

  /// Username (email)
  final String username;

  /// Email
  final String email;

  /// Roles
  final Set<String> roles;
}
