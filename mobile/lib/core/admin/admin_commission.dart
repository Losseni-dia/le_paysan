class CommissionRate {
  final int id;
  final double rate;
  final bool active;
  final DateTime? createdAt;

  CommissionRate({
    required this.id,
    required this.rate,
    required this.active,
    required this.createdAt,
  });

  factory CommissionRate.fromJson(Map<String, dynamic> json) {
    return CommissionRate(
      id: json['id'] as int,
      rate: (json['rate'] as num).toDouble(),
      active: json['active'] as bool,
      createdAt: json['createdAt'] != null ? DateTime.tryParse(json['createdAt'] as String) : null,
    );
  }
}
