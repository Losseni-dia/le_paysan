import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'review.dart';

class ReviewService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<ProductReview>> listForProduct(int productId) async {
    final response = await _dio.get('/products/$productId/avis');
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content.map((e) => ProductReview.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<RatingSummary> getSummary(int productId) async {
    final response = await _dio.get('/products/$productId/avis/resume');
    return RatingSummary.fromJson(response.data as Map<String, dynamic>);
  }

  Future<ProductReview?> getMyReview(int productId) async {
    final response = await _dio.get('/products/$productId/avis/moi');
    if (response.statusCode == 204 || response.data == null) return null;
    return ProductReview.fromJson(response.data as Map<String, dynamic>);
  }

  Future<ReviewEligibility> getEligibility(int productId) async {
    final response = await _dio.get('/products/$productId/avis/eligibilite');
    return ReviewEligibility.fromJson(response.data as Map<String, dynamic>);
  }

  Future<ProductReview> submit(int productId, {required int rating, String? comment}) async {
    final response = await _dio.post(
      '/products/$productId/avis',
      data: {
        'rating': rating,
        if (comment != null && comment.isNotEmpty) 'comment': comment,
      },
    );
    return ProductReview.fromJson(response.data as Map<String, dynamic>);
  }
}
