class RatingSummary {
  final double average;
  final int count;

  RatingSummary({required this.average, required this.count});

  factory RatingSummary.fromJson(Map<String, dynamic> json) {
    return RatingSummary(
      average: (json['average'] as num).toDouble(),
      count: json['count'] as int,
    );
  }
}

class ProductReview {
  final int id;
  final int productId;
  final int buyerUserId;
  final String buyerFullName;
  final int rating;
  final String? comment;
  final DateTime? createdAt;

  ProductReview({
    required this.id,
    required this.productId,
    required this.buyerUserId,
    required this.buyerFullName,
    required this.rating,
    required this.comment,
    required this.createdAt,
  });

  factory ProductReview.fromJson(Map<String, dynamic> json) {
    return ProductReview(
      id: json['id'] as int,
      productId: json['productId'] as int,
      buyerUserId: json['buyerUserId'] as int,
      buyerFullName: json['buyerFullName'] as String,
      rating: json['rating'] as int,
      comment: json['comment'] as String?,
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
    );
  }
}

/// "NOT_PURCHASED" : l'acheteur doit avoir recu ce produit (commande livree) pour pouvoir noter.
class ReviewEligibility {
  final bool eligible;
  final String? reason;

  ReviewEligibility({required this.eligible, required this.reason});

  factory ReviewEligibility.fromJson(Map<String, dynamic> json) {
    return ReviewEligibility(
      eligible: json['eligible'] as bool,
      reason: json['reason'] as String?,
    );
  }
}
