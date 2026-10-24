class EligibleLivreur {
  final int id;
  final String firstName;
  final String lastName;
  final String? phone;
  final String? city;
  final double? distanceKm;

  EligibleLivreur({
    required this.id,
    required this.firstName,
    required this.lastName,
    required this.phone,
    required this.city,
    required this.distanceKm,
  });

  factory EligibleLivreur.fromJson(Map<String, dynamic> json) {
    return EligibleLivreur(
      id: json['id'] as int,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      phone: json['phone'] as String?,
      city: json['city'] as String?,
      distanceKm: (json['distanceKm'] as num?)?.toDouble(),
    );
  }
}
