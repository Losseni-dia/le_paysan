class OrderItem {
  final int productId;
  final String productName;
  final double unitPrice;
  final int quantity;
  final String deliveryMode;
  final double deliveryPrice;
  final double lineSubtotal;

  OrderItem({
    required this.productId,
    required this.productName,
    required this.unitPrice,
    required this.quantity,
    required this.deliveryMode,
    required this.deliveryPrice,
    required this.lineSubtotal,
  });

  factory OrderItem.fromJson(Map<String, dynamic> json) {
    return OrderItem(
      productId: json['productId'] as int,
      productName: json['productName'] as String,
      unitPrice: (json['unitPrice'] as num).toDouble(),
      quantity: json['quantity'] as int,
      deliveryMode: json['deliveryMode'] as String,
      deliveryPrice: (json['deliveryPrice'] as num?)?.toDouble() ?? 0,
      lineSubtotal: (json['lineSubtotal'] as num).toDouble(),
    );
  }
}

class Order {
  final int id;
  final String orderNumber;
  final int shopId;
  final String shopName;
  final String status;
  final double subtotalAmount;
  final double deliveryAmount;
  final double totalAmount;
  final List<OrderItem> items;
  final DateTime? createdAt;

  Order({
    required this.id,
    required this.orderNumber,
    required this.shopId,
    required this.shopName,
    required this.status,
    required this.subtotalAmount,
    required this.deliveryAmount,
    required this.totalAmount,
    required this.items,
    this.createdAt,
  });

  factory Order.fromJson(Map<String, dynamic> json) {
    return Order(
      id: json['id'] as int,
      orderNumber: json['orderNumber'] as String,
      shopId: json['shopId'] as int,
      shopName: json['shopName'] as String,
      status: json['status'] as String,
      subtotalAmount: (json['subtotalAmount'] as num).toDouble(),
      deliveryAmount: (json['deliveryAmount'] as num).toDouble(),
      totalAmount: (json['totalAmount'] as num).toDouble(),
      items: (json['items'] as List<dynamic>)
          .map((e) => OrderItem.fromJson(e as Map<String, dynamic>))
          .toList(),
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
    );
  }
}
