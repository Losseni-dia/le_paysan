import 'package:dio/dio.dart';

import '../api/api_client.dart';
import '../livreur/livreur_order.dart';

class AdminOrderService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<LivreurOrder>> listForAdmin(String? status) async {
    final response = await _dio.get(
      '/admin/orders',
      queryParameters: {'status': ?status},
    );
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content
        .map((e) => LivreurOrder.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<LivreurOrder> getDetail(int id) async {
    final response = await _dio.get('/admin/orders/$id');
    return LivreurOrder.fromJson(response.data as Map<String, dynamic>);
  }
}
