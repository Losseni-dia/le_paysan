import 'package:dio/dio.dart';

import '../api/api_client.dart';

class ShopRequestService {
  final Dio _dio = ApiClient.instance.dio;

  Future<void> submitRequest(FormData data) async {
    await _dio.post('/shops/requests', data: data);
  }
}
