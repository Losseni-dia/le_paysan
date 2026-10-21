import 'package:dio/dio.dart';

import '../api/api_client.dart';
import '../api/api_config.dart';
import 'product.dart';

class ProductService {
  final Dio _dio = ApiClient.instance.dio;

  Future<ProductPage> listPublic({String? search, int page = 0}) async {
    final response = await _dio.get(
      '/products',
      queryParameters: {
        if (search != null && search.isNotEmpty) 'search': search,
        'page': page,
      },
    );
    return ProductPage.fromJson(response.data as Map<String, dynamic>);
  }

  Future<Product> getPublic(int id) async {
    final response = await _dio.get('/products/$id');
    return Product.fromJson(response.data as Map<String, dynamic>);
  }

  /// Construit l'URL absolue d'une image a partir du chemin relatif stocke en base
  /// (ex. "products/uuid.jpg"), cf. backend FileController.
  static String imageUrl(String relativePath) =>
      '${ApiConfig.baseUrl}/files/$relativePath';
}
