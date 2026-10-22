class LivreurOrderItem {
  final String productName;
  final double unitPrice;
  final int quantity;
  final String deliveryMode;
  final String? deliveryAddress;
  final double lineSubtotal;

  LivreurOrderItem({
    required this.productName,
    required this.unitPrice,
    required this.quantity,
    required this.deliveryMode,
    required this.deliveryAddress,
    required this.lineSubtotal,
  });

  factory LivreurOrderItem.fromJson(Map<String, dynamic> json) {
    return LivreurOrderItem(
      productName: json['productName'] as String,
      unitPrice: (json['unitPrice'] as num).toDouble(),
      quantity: json['quantity'] as int,
      deliveryMode: json['deliveryMode'] as String,
      deliveryAddress: json['deliveryAddress'] as String?,
      lineSubtotal: (json['lineSubtotal'] as num).toDouble(),
    );
  }
}

class OrderStatusHistoryEntry {
  final String status;
  final String changedBy;
  final String? note;
  final String proofType;
  final String? proofUrl;
  final DateTime? changedAt;

  OrderStatusHistoryEntry({
    required this.status,
    required this.changedBy,
    required this.note,
    required this.proofType,
    required this.proofUrl,
    required this.changedAt,
  });

  factory OrderStatusHistoryEntry.fromJson(Map<String, dynamic> json) {
    return OrderStatusHistoryEntry(
      status: json['status'] as String,
      changedBy: json['changedBy'] as String,
      note: json['note'] as String?,
      proofType: json['proofType'] as String? ?? 'NONE',
      proofUrl: json['proofUrl'] as String?,
      changedAt: json['changedAt'] != null
          ? DateTime.tryParse(json['changedAt'] as String)
          : null,
    );
  }
}

class LivreurOrder {
  final int id;
  final String orderNumber;
  final String shopName;
  final String buyerFullName;
  final String status;
  final double totalAmount;
  final List<LivreurOrderItem> items;
  final List<OrderStatusHistoryEntry> historique;
  final DateTime? createdAt;

  LivreurOrder({
    required this.id,
    required this.orderNumber,
    required this.shopName,
    required this.buyerFullName,
    required this.status,
    required this.totalAmount,
    required this.items,
    this.historique = const [],
    this.createdAt,
  });

  /// Pour la barre de recherche (numero de commande, acheteur, produit).
  bool matchesSearch(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) return true;
    if (orderNumber.toLowerCase().contains(q)) return true;
    if (buyerFullName.toLowerCase().contains(q)) return true;
    return items.any((item) => item.productName.toLowerCase().contains(q));
  }

  /// Derniere entree d'historique portant une preuve de livraison (signature/photo), s'il y en a une.
  OrderStatusHistoryEntry? get deliveryProof {
    for (final entry in historique.reversed) {
      if (entry.proofUrl != null) return entry;
    }
    return null;
  }

  factory LivreurOrder.fromJson(Map<String, dynamic> json) {
    return LivreurOrder(
      id: json['id'] as int,
      orderNumber: json['orderNumber'] as String,
      shopName: json['shopName'] as String,
      buyerFullName: json['buyerFullName'] as String,
      status: json['status'] as String,
      totalAmount: (json['totalAmount'] as num).toDouble(),
      items: (json['items'] as List<dynamic>)
          .map((e) => LivreurOrderItem.fromJson(e as Map<String, dynamic>))
          .toList(),
      historique: (json['historique'] as List<dynamic>? ?? [])
          .map((e) => OrderStatusHistoryEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
    );
  }
}
