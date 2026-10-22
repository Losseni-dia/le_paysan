import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'admin_dashboard.dart';

class AdminDashboardService {
  final Dio _dio = ApiClient.instance.dio;

  Future<AdminDashboard> getDashboard() async {
    final response = await _dio.get('/admin/dashboard');
    return AdminDashboard.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AdminStats> getStats() async {
    final response = await _dio.get('/admin/statistiques');
    return AdminStats.fromJson(response.data as Map<String, dynamic>);
  }
}
