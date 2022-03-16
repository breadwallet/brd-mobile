class User {
  User(this.firstName, this.lastName, this.email, this.phone);

  String firstName;
  String lastName;
  String email;
  String phone;

  @override
  String toString() {
    return 'User{firstName: $firstName, lastName: $lastName,'
        ' email: $email, phone: $phone}';
  }
}
