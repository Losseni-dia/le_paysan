import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'admin_livreur.dart';

class AdminLivreurService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<AdminLivreur>> listForAdmin(String? kycStatus) async {
    final response = await _dio.get(
      '/admin/livreurs',
      queryParameters: {'kycStatus': ?kycStatus},
    );
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content
        .map((e) => AdminLivreur.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<AdminLivreur> getDetail(int id) async {
    final response = await _dio.get('/admin/livreurs/$id');
    return AdminLivreur.fromJson(response.data as Map<String, dynamic>);
  }

  Future<void> validateKyc(int id) async {
    await _dio.post('/admin/livreurs/$id/valider-kyc');
  }

  Future<void> rejectKyc(int id, String reason) async {
    await _dio.post('/admin/livreurs/$id/rejeter-kyc', data: {'reason': reason});
  }

  Future<void> suspend(int id, String reason) async {
    await _dio.post('/admin/livreurs/$id/suspend', data: {'reason': reason});
  }

  Future<void> restore(int id) async {
    await _dio.post('/admin/livreurs/$id/restore');
  }
}
