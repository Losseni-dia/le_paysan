import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'admin_review.dart';

class AdminReviewService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<AdminReview>> listProductReviews(String? status) async {
    final response = await _dio.get('/admin/avis/produits', queryParameters: {'status': ?status});
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content.map((e) => AdminReview.fromProductJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<AdminReview>> listShopReviews(String? status) async {
    final response = await _dio.get('/admin/avis/boutiques', queryParameters: {'status': ?status});
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content.map((e) => AdminReview.fromShopJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> moderateProduct(int id, String status, String? reason) async {
    await _dio.patch('/admin/avis/produits/$id', data: {'status': status, 'reason': ?reason});
  }

  Future<void> moderateShop(int id, String status, String? reason) async {
    await _dio.patch('/admin/avis/boutiques/$id', data: {'status': status, 'reason': ?reason});
  }
}
