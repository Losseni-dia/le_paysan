import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'admin_payout.dart';

class AdminPayoutService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<AdminPayout>> listForAdmin(String status) async {
    final response = await _dio.get('/admin/payouts', queryParameters: {'status': status});
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content.map((e) => AdminPayout.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> retry(int id) async {
    await _dio.post('/admin/payouts/$id/retry');
  }
}
