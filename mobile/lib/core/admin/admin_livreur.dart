class AdminLivreur {
  final int id;
  final String username;
  final String firstName;
  final String lastName;
  final String? phone;
  final String? city;
  final String? photoUrl;
  final String? idDocumentFrontUrl;
  final String? idDocumentBackUrl;
  final String? selfieWithIdUrl;
  final String kycStatus;
  final bool enabled;
  final bool available;
  final DateTime? createdAt;

  AdminLivreur({
    required this.id,
    required this.username,
    required this.firstName,
    required this.lastName,
    required this.phone,
    required this.city,
    required this.photoUrl,
    required this.idDocumentFrontUrl,
    required this.idDocumentBackUrl,
    required this.selfieWithIdUrl,
    required this.kycStatus,
    required this.enabled,
    required this.available,
    required this.createdAt,
  });

  bool matchesSearch(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) return true;
    if ('$firstName $lastName'.toLowerCase().contains(q)) return true;
    if (username.toLowerCase().contains(q)) return true;
    return (city ?? '').toLowerCase().contains(q);
  }

  factory AdminLivreur.fromJson(Map<String, dynamic> json) {
    return AdminLivreur(
      id: json['id'] as int,
      username: json['username'] as String,
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      phone: json['phone'] as String?,
      city: json['city'] as String?,
      photoUrl: json['photoUrl'] as String?,
      idDocumentFrontUrl: json['idDocumentFrontUrl'] as String?,
      idDocumentBackUrl: json['idDocumentBackUrl'] as String?,
      selfieWithIdUrl: json['selfieWithIdUrl'] as String?,
      kycStatus: json['kycStatus'] as String,
      enabled: json['enabled'] as bool,
      available: json['available'] as bool,
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
    );
  }
}
