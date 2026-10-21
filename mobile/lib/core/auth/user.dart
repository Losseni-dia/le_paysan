class AppUser {
  final int id;
  final String username;
  final String? email;
  final String firstName;
  final String lastName;
  final String? phone;
  final bool hasSecuritySelfie;
  final List<String> roles;

  AppUser({
    required this.id,
    required this.username,
    required this.email,
    required this.firstName,
    required this.lastName,
    required this.phone,
    required this.hasSecuritySelfie,
    required this.roles,
  });

  factory AppUser.fromJson(Map<String, dynamic> json) {
    return AppUser(
      id: json['id'] as int,
      username: json['username'] as String,
      email: json['email'] as String?,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      phone: json['phone'] as String?,
      hasSecuritySelfie: json['hasSecuritySelfie'] as bool? ?? false,
      roles: (json['roles'] as List<dynamic>).map((r) => r as String).toList(),
    );
  }

  bool hasRole(String role) => roles.contains(role);
}
