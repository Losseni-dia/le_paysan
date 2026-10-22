import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'admin_category.dart';

class AdminCategoryService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<AdminCategory>> listForAdmin() async {
    final response = await _dio.get('/admin/categories');
    return (response.data as List<dynamic>)
        .map((e) => AdminCategory.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<AdminCategory> validate(int id) async {
    final response = await _dio.post('/admin/categories/$id/validate');
    return AdminCategory.fromJson(response.data as Map<String, dynamic>);
  }

  /// Creation directe par l'admin (cf. POST /api/v1/categories, distinct de
  /// l'admin/categories/{id}/validate ci-dessus) : la categorie est VALIDE immediatement.
  Future<AdminCategory> create(String name) async {
    final response = await _dio.post('/categories', data: {'name': name});
    return AdminCategory.fromJson(response.data as Map<String, dynamic>);
  }

  Future<AdminCategory> update(int id, String name) async {
    final response = await _dio.patch('/admin/categories/$id', data: {'name': name});
    return AdminCategory.fromJson(response.data as Map<String, dynamic>);
  }

  Future<void> delete(int id) async {
    await _dio.delete('/admin/categories/$id');
  }
}
