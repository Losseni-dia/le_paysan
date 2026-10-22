class AdminProductDeliveryOption {
  final String mode;
  final String? pickupLocation;
  final double? deliveryPrice;

  AdminProductDeliveryOption({
    required this.mode,
    required this.pickupLocation,
    required this.deliveryPrice,
  });

  factory AdminProductDeliveryOption.fromJson(Map<String, dynamic> json) {
    return AdminProductDeliveryOption(
      mode: json['mode'] as String,
      pickupLocation: json['pickupLocation'] as String?,
      deliveryPrice: (json['deliveryPrice'] as num?)?.toDouble(),
    );
  }
}

class AdminProduct {
  final int id;
  final String shopName;
  final String categoryName;
  final String name;
  final String? description;
  final String unit;
  final double unitPrice;
  final int stockQuantity;
  final String status;
  final List<String> imageUrls;
  final List<AdminProductDeliveryOption> deliveryOptions;
  final DateTime? createdAt;

  AdminProduct({
    required this.id,
    required this.shopName,
    required this.categoryName,
    required this.name,
    required this.description,
    required this.unit,
    required this.unitPrice,
    required this.stockQuantity,
    required this.status,
    required this.imageUrls,
    required this.deliveryOptions,
    required this.createdAt,
  });

  bool matchesSearch(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) return true;
    return name.toLowerCase().contains(q) || shopName.toLowerCase().contains(q);
  }

  factory AdminProduct.fromJson(Map<String, dynamic> json) {
    return AdminProduct(
      id: json['id'] as int,
      shopName: json['shopName'] as String,
      categoryName: json['categoryName'] as String,
      name: json['name'] as String,
      description: json['description'] as String?,
      unit: json['unit'] as String,
      unitPrice: (json['unitPrice'] as num).toDouble(),
      stockQuantity: json['stockQuantity'] as int,
      status: json['status'] as String,
      imageUrls: (json['imageUrls'] as List<dynamic>? ?? []).map((e) => e as String).toList(),
      deliveryOptions: (json['deliveryOptions'] as List<dynamic>? ?? [])
          .map((e) => AdminProductDeliveryOption.fromJson(e as Map<String, dynamic>))
          .toList(),
      createdAt: json['createdAt'] != null ? DateTime.tryParse(json['createdAt'] as String) : null,
    );
  }
}
