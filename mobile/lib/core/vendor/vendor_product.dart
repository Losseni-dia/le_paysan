class VendorProduct {
  final int id;
  final String name;
  final String unit;
  final double unitPrice;
  final int stockQuantity;
  final String status;
  final List<String> imageUrls;

  VendorProduct({
    required this.id,
    required this.name,
    required this.unit,
    required this.unitPrice,
    required this.stockQuantity,
    required this.status,
    required this.imageUrls,
  });

  factory VendorProduct.fromJson(Map<String, dynamic> json) {
    return VendorProduct(
      id: json['id'] as int,
      name: json['name'] as String,
      unit: json['unit'] as String,
      unitPrice: (json['unitPrice'] as num).toDouble(),
      stockQuantity: json['stockQuantity'] as int,
      status: json['status'] as String,
      imageUrls: (json['imageUrls'] as List<dynamic>? ?? [])
          .map((e) => e as String)
          .toList(),
    );
  }

  bool matchesSearch(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) return true;
    return name.toLowerCase().contains(q);
  }
}
