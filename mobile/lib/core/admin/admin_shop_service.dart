import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'admin_shop.dart';

class AdminShopService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<AdminShopDetail>> listForAdmin(String status) async {
    final response = await _dio.get(
      '/admin/shops',
      queryParameters: {'status': status},
    );
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content
        .map((e) => AdminShopDetail.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<AdminShopDetail> getDetail(int id) async {
    final response = await _dio.get('/admin/shops/$id');
    return AdminShopDetail.fromJson(response.data as Map<String, dynamic>);
  }

  Future<void> validate(int id) async {
    await _dio.post('/admin/shops/$id/validate');
  }

  Future<void> reject(int id, String reason) async {
    await _dio.post('/admin/shops/$id/reject', data: {'reason': reason});
  }

  Future<void> suspend(int id, String reason) async {
    await _dio.post('/admin/shops/$id/suspend', data: {'reason': reason});
  }

  Future<void> restore(int id) async {
    await _dio.post('/admin/shops/$id/restore');
  }
}
