import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'vendor_product.dart';

class VendorProductService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<VendorProduct>> listMine() async {
    final response = await _dio.get('/vendor/products');
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content
        .map((e) => VendorProduct.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<VendorProduct> togglePublishState(int id) async {
    final response = await _dio.patch('/vendor/products/$id/publish-state');
    return VendorProduct.fromJson(response.data as Map<String, dynamic>);
  }
}
