class Product {
  final int id;
  final int shopId;
  final String shopName;
  final String? shopCity;
  final String name;
  final String? description;
  final String unit;
  final double unitPrice;
  final int stockQuantity;
  final List<String> imageUrls;
  final double? ratingAverage;
  final int ratingCount;

  Product({
    required this.id,
    required this.shopId,
    required this.shopName,
    required this.shopCity,
    required this.name,
    required this.description,
    required this.unit,
    required this.unitPrice,
    required this.stockQuantity,
    required this.imageUrls,
    required this.ratingAverage,
    required this.ratingCount,
  });

  factory Product.fromJson(Map<String, dynamic> json) {
    return Product(
      id: json['id'] as int,
      shopId: json['shopId'] as int,
      shopName: json['shopName'] as String,
      shopCity: json['shopCity'] as String?,
      name: json['name'] as String,
      description: json['description'] as String?,
      unit: json['unit'] as String,
      unitPrice: (json['unitPrice'] as num).toDouble(),
      stockQuantity: json['stockQuantity'] as int,
      imageUrls: (json['imageUrls'] as List<dynamic>).map((e) => e as String).toList(),
      ratingAverage: (json['ratingAverage'] as num?)?.toDouble(),
      ratingCount: (json['ratingCount'] as num?)?.toInt() ?? 0,
    );
  }
}

class ProductPage {
  final List<Product> content;
  final int totalPages;
  final int number;

  ProductPage({required this.content, required this.totalPages, required this.number});

  factory ProductPage.fromJson(Map<String, dynamic> json) {
    return ProductPage(
      content: (json['content'] as List<dynamic>)
          .map((e) => Product.fromJson(e as Map<String, dynamic>))
          .toList(),
      totalPages: json['totalPages'] as int,
      number: json['number'] as int,
    );
  }
}
