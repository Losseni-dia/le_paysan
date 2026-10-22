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

class LivreurOrder {
  final int id;
  final String orderNumber;
  final String shopName;
  final String buyerFullName;
  final String status;
  final double totalAmount;
  final List<LivreurOrderItem> items;
  final DateTime? createdAt;

  LivreurOrder({
    required this.id,
    required this.orderNumber,
    required this.shopName,
    required this.buyerFullName,
    required this.status,
    required this.totalAmount,
    required this.items,
    this.createdAt,
  });

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
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
    );
  }
}
