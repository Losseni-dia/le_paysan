class AdminPayout {
  final int id;
  final String orderNumber;
  final String shopName;
  final double amount;
  final String status;
  final String? failureReason;
  final DateTime? createdAt;

  AdminPayout({
    required this.id,
    required this.orderNumber,
    required this.shopName,
    required this.amount,
    required this.status,
    required this.failureReason,
    required this.createdAt,
  });

  bool matchesSearch(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) return true;
    return orderNumber.toLowerCase().contains(q) || shopName.toLowerCase().contains(q);
  }

  factory AdminPayout.fromJson(Map<String, dynamic> json) {
    return AdminPayout(
      id: json['id'] as int,
      orderNumber: json['orderNumber'] as String,
      shopName: json['shopName'] as String,
      amount: (json['amount'] as num).toDouble(),
      status: json['status'] as String,
      failureReason: json['failureReason'] as String?,
      createdAt: json['createdAt'] != null ? DateTime.tryParse(json['createdAt'] as String) : null,
    );
  }
}
