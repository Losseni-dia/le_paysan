class AdminReview {
  final int id;
  final String subjectName;
  final String buyerFullName;
  final int rating;
  final String? comment;
  final String status;
  final String? moderationReason;
  final DateTime? createdAt;

  AdminReview({
    required this.id,
    required this.subjectName,
    required this.buyerFullName,
    required this.rating,
    required this.comment,
    required this.status,
    required this.moderationReason,
    required this.createdAt,
  });

  bool matchesSearch(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) return true;
    if (subjectName.toLowerCase().contains(q)) return true;
    if (buyerFullName.toLowerCase().contains(q)) return true;
    return (comment ?? '').toLowerCase().contains(q);
  }

  factory AdminReview.fromProductJson(Map<String, dynamic> json) {
    return AdminReview(
      id: json['id'] as int,
      subjectName: json['productName'] as String,
      buyerFullName: json['buyerFullName'] as String,
      rating: json['rating'] as int,
      comment: json['comment'] as String?,
      status: json['status'] as String,
      moderationReason: json['moderationReason'] as String?,
      createdAt: json['createdAt'] != null ? DateTime.tryParse(json['createdAt'] as String) : null,
    );
  }

  factory AdminReview.fromShopJson(Map<String, dynamic> json) {
    return AdminReview(
      id: json['id'] as int,
      subjectName: json['shopName'] as String,
      buyerFullName: json['buyerFullName'] as String,
      rating: json['rating'] as int,
      comment: json['comment'] as String?,
      status: json['status'] as String,
      moderationReason: json['moderationReason'] as String?,
      createdAt: json['createdAt'] != null ? DateTime.tryParse(json['createdAt'] as String) : null,
    );
  }
}
