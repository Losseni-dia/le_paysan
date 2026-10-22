import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'admin_user.dart';

class AdminUserService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<AdminUser>> list({String? search, String? role}) async {
    final response = await _dio.get(
      '/admin/users',
      queryParameters: {'search': ?search, 'role': ?role},
    );
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content.map((e) => AdminUser.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<AdminUser> setEnabled(int id, bool enabled) async {
    final response = await _dio.patch('/admin/users/$id/enabled', data: {'enabled': enabled});
    return AdminUser.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AdminUser> setRoles(int id, List<String> roles) async {
    final response = await _dio.patch('/admin/users/$id/roles', data: {'roles': roles});
    return AdminUser.fromJson(response.data as Map<String, dynamic>);
  }
}
