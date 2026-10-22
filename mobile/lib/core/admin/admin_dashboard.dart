class AdminDashboard {
  final int usersTotal;
  final int shopsValidated;
  final int shopsPending;
  final int productsPending;
  final int ordersPendingPayment;
  final int payoutsFailed;
  final int reviewsTotal;

  AdminDashboard({
    required this.usersTotal,
    required this.shopsValidated,
    required this.shopsPending,
    required this.productsPending,
    required this.ordersPendingPayment,
    required this.payoutsFailed,
    required this.reviewsTotal,
  });

  factory AdminDashboard.fromJson(Map<String, dynamic> json) {
    return AdminDashboard(
      usersTotal: json['utilisateursTotal'] as int,
      shopsValidated: json['boutiquesValidees'] as int,
      shopsPending: json['boutiquesEnAttente'] as int,
      productsPending: json['produitsEnAttente'] as int,
      ordersPendingPayment: json['commandesEnAttentePaiement'] as int,
      payoutsFailed: json['reversementsEnEchec'] as int,
      reviewsTotal: json['avisTotal'] as int,
    );
  }
}

class ShopRevenue {
  final String shopName;
  final double revenue;

  ShopRevenue({required this.shopName, required this.revenue});

  factory ShopRevenue.fromJson(Map<String, dynamic> json) {
    return ShopRevenue(
      shopName: json['shopName'] as String,
      revenue: (json['revenue'] as num).toDouble(),
    );
  }
}

class AdminStats {
  final double totalRevenue;
  final double totalCommission;
  final int activeShops;
  final Map<String, int> ordersByStatus;
  final List<ShopRevenue> topShops;

  AdminStats({
    required this.totalRevenue,
    required this.totalCommission,
    required this.activeShops,
    required this.ordersByStatus,
    required this.topShops,
  });

  factory AdminStats.fromJson(Map<String, dynamic> json) {
    return AdminStats(
      totalRevenue: (json['chiffreAffairesTotal'] as num).toDouble(),
      totalCommission: (json['commissionTotale'] as num).toDouble(),
      activeShops: json['boutiquesActives'] as int,
      ordersByStatus: (json['commandesParStatut'] as Map<String, dynamic>)
          .map((key, value) => MapEntry(key, value as int)),
      topShops: (json['topBoutiques'] as List<dynamic>)
          .map((e) => ShopRevenue.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}
