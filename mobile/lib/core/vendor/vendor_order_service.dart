import 'dart:typed_data';

import 'package:dio/dio.dart';

import '../api/api_client.dart';
import '../livreur/livreur_order.dart';
import '../livreur/livreur_profile.dart';

class VendorOrderService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<LivreurOrder>> listOrders() async {
    final response = await _dio.get('/vendor/orders');
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content
        .map((e) => LivreurOrder.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<LivreurOrder> getOrder(int id) async {
    final response = await _dio.get('/vendor/orders/$id');
    return LivreurOrder.fromJson(response.data as Map<String, dynamic>);
  }

  Future<List<LivreurProfile>> listLivreurs() async {
    final response = await _dio.get('/vendor/livreurs');
    return (response.data as List<dynamic>)
        .map((e) => LivreurProfile.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<LivreurOrder> advanceStatus(
    int id, {
    int? livreurId,
    String? proofType,
    Uint8List? proofBytes,
    String? proofFilename,
  }) async {
    final formData = FormData.fromMap({
      'livreurId': ?livreurId,
      if (proofType != null && proofBytes != null) ...{
        'proofType': proofType,
        'proof': MultipartFile.fromBytes(proofBytes, filename: proofFilename ?? 'preuve'),
      },
    });
    final response = await _dio.patch('/vendor/orders/$id/status', data: formData);
    return LivreurOrder.fromJson(response.data as Map<String, dynamic>);
  }
}
