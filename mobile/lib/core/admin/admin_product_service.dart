import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'admin_product.dart';

class AdminProductService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<AdminProduct>> listForAdmin(String status) async {
    final response = await _dio.get(
      '/admin/products',
      queryParameters: {'status': status},
    );
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content.map((e) => AdminProduct.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<AdminProduct> getDetail(int id) async {
    final response = await _dio.get('/admin/products/$id');
    return AdminProduct.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AdminProduct> validate(int id) async {
    final response = await _dio.post('/admin/products/$id/validate');
    return AdminProduct.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AdminProduct> reject(int id, String reason) async {
    final response = await _dio.post('/admin/products/$id/reject', data: {'reason': reason});
    return AdminProduct.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AdminProduct> togglePublishState(int id) async {
    final response = await _dio.patch('/admin/products/$id/publish-state');
    return AdminProduct.fromJson(response.data as Map<String, dynamic>);
  }
}
