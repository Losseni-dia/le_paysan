import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'admin_commission.dart';

class AdminCommissionService {
  final Dio _dio = ApiClient.instance.dio;

  Future<CommissionRate> getCurrent() async {
    final response = await _dio.get('/admin/commission');
    return CommissionRate.fromJson(response.data as Map<String, dynamic>);
  }

  Future<List<CommissionRate>> getHistory() async {
    final response = await _dio.get('/admin/commission/historique');
    return (response.data as List<dynamic>)
        .map((e) => CommissionRate.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<CommissionRate> updateRate(double rate) async {
    final response = await _dio.post('/admin/commission', data: {'rate': rate});
    return CommissionRate.fromJson(response.data as Map<String, dynamic>);
  }
}
