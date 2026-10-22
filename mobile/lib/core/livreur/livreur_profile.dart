class LivreurProfile {
  final int id;
  final String firstName;
  final String lastName;
  final String? phone;
  final String kycStatus;
  final bool enabled;
  final bool available;
  final int deliveryCount;

  LivreurProfile({
    required this.id,
    required this.firstName,
    required this.lastName,
    required this.phone,
    required this.kycStatus,
    required this.enabled,
    required this.available,
    required this.deliveryCount,
  });

  factory LivreurProfile.fromJson(Map<String, dynamic> json) {
    return LivreurProfile(
      id: json['id'] as int,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      phone: json['phone'] as String?,
      kycStatus: json['kycStatus'] as String,
      enabled: json['enabled'] as bool,
      available: json['available'] as bool,
      deliveryCount: json['deliveryCount'] as int,
    );
  }
}
