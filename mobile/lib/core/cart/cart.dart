class CartItem {
  final int productId;
  final String productName;
  final String? imageUrl;
  final int shopId;
  final String shopName;
  final double unitPrice;
  final int quantity;
  final String deliveryMode;
  final double deliveryPrice;
  final String? deliveryAddress;
  final String? deliveryCityName;
  final double lineSubtotal;
  final List<String> availableDeliveryModes;
  final int stockAvailable;
  final bool available;
  final bool deliveryOutOfRange;

  CartItem({
    required this.productId,
    required this.productName,
    required this.imageUrl,
    required this.shopId,
    required this.shopName,
    required this.unitPrice,
    required this.quantity,
    required this.deliveryMode,
    required this.deliveryPrice,
    required this.deliveryAddress,
    required this.deliveryCityName,
    required this.lineSubtotal,
    required this.availableDeliveryModes,
    required this.stockAvailable,
    required this.available,
    required this.deliveryOutOfRange,
  });

  factory CartItem.fromJson(Map<String, dynamic> json) {
    return CartItem(
      productId: json['productId'] as int,
      productName: json['productName'] as String,
      imageUrl: json['imageUrl'] as String?,
      shopId: json['shopId'] as int,
      shopName: json['shopName'] as String,
      unitPrice: (json['unitPrice'] as num).toDouble(),
      quantity: json['quantity'] as int,
      deliveryMode: json['deliveryMode'] as String,
      deliveryPrice: (json['deliveryPrice'] as num?)?.toDouble() ?? 0,
      deliveryAddress: json['deliveryAddress'] as String?,
      deliveryCityName: json['deliveryCityName'] as String?,
      lineSubtotal: (json['lineSubtotal'] as num).toDouble(),
      availableDeliveryModes:
          (json['availableDeliveryModes'] as List<dynamic>? ?? [])
              .map((e) => e as String)
              .toList(),
      stockAvailable: json['stockAvailable'] as int,
      available: json['available'] as bool,
      deliveryOutOfRange: json['deliveryOutOfRange'] as bool,
    );
  }
}

class Cart {
  final List<CartItem> items;
  final double subtotalAmount;
  final double deliveryAmount;
  final double totalAmount;

  Cart({
    required this.items,
    required this.subtotalAmount,
    required this.deliveryAmount,
    required this.totalAmount,
  });

  factory Cart.fromJson(Map<String, dynamic> json) {
    return Cart(
      items: (json['items'] as List<dynamic>)
          .map((e) => CartItem.fromJson(e as Map<String, dynamic>))
          .toList(),
      subtotalAmount: (json['subtotalAmount'] as num).toDouble(),
      deliveryAmount: (json['deliveryAmount'] as num).toDouble(),
      totalAmount: (json['totalAmount'] as num).toDouble(),
    );
  }
}
