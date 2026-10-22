class LivreurProfile {
  final int id;
  final String username;
  final String firstName;
  final String lastName;
  final String? phone;
  final String? city;
  final String kycStatus;
  final bool enabled;
  final bool available;
  final int deliveryCount;

  LivreurProfile({
    required this.id,
    required this.username,
    required this.firstName,
    required this.lastName,
    required this.phone,
    required this.city,
    required this.kycStatus,
    required this.enabled,
    required this.available,
    required this.deliveryCount,
  });

  factory LivreurProfile.fromJson(Map<String, dynamic> json) {
    return LivreurProfile(
      id: json['id'] as int,
      username: json['username'] as String? ?? '',
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      phone: json['phone'] as String?,
      city: json['city'] as String?,
      kycStatus: json['kycStatus'] as String,
      enabled: json['enabled'] as bool,
      available: json['available'] as bool,
      deliveryCount: json['deliveryCount'] as int,
    );
  }

  bool matchesSearch(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) return true;
    if ('$firstName $lastName'.toLowerCase().contains(q)) return true;
    if (username.toLowerCase().contains(q)) return true;
    return (city ?? '').toLowerCase().contains(q);
  }
}
