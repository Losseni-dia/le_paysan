import 'package:dio/dio.dart';

import '../api/api_client.dart';
import '../livreur/livreur_profile.dart';

class VendorLivreurService {
  final Dio _dio = ApiClient.instance.dio;

  Future<List<LivreurProfile>> listMine() async {
    final response = await _dio.get('/vendor/livreurs');
    return (response.data as List<dynamic>)
        .map((e) => LivreurProfile.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<LivreurProfile> setEnabled(int id, bool enabled) async {
    final response = await _dio.patch(
      '/vendor/livreurs/$id/enabled',
      data: {'enabled': enabled},
    );
    return LivreurProfile.fromJson(response.data as Map<String, dynamic>);
  }
}
