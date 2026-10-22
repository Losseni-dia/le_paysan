class AdminUser {
  final int id;
  final String username;
  final String? email;
  final String firstName;
  final String lastName;
  final String? phone;
  final bool enabled;
  final List<String> roles;
  final DateTime? createdAt;

  AdminUser({
    required this.id,
    required this.username,
    required this.email,
    required this.firstName,
    required this.lastName,
    required this.phone,
    required this.enabled,
    required this.roles,
    required this.createdAt,
  });

  factory AdminUser.fromJson(Map<String, dynamic> json) {
    return AdminUser(
      id: json['id'] as int,
      username: json['username'] as String,
      email: json['email'] as String?,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      phone: json['phone'] as String?,
      enabled: json['enabled'] as bool,
      roles: (json['roles'] as List<dynamic>).map((e) => e as String).toList(),
      createdAt: json['createdAt'] != null ? DateTime.tryParse(json['createdAt'] as String) : null,
    );
  }
}
