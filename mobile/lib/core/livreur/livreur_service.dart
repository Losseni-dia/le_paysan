import 'package:dio/dio.dart';
import 'package:image_picker/image_picker.dart';

import '../api/api_client.dart';
import 'livreur_order.dart';
import 'livreur_profile.dart';

class LivreurService {
  final Dio _dio = ApiClient.instance.dio;

  Future<LivreurProfile> getSelf() async {
    final response = await _dio.get('/livreur/moi');
    return LivreurProfile.fromJson(response.data as Map<String, dynamic>);
  }

  Future<LivreurProfile> setAvailability(bool available) async {
    final response = await _dio.patch(
      '/livreur/disponibilite',
      data: {'available': available},
    );
    return LivreurProfile.fromJson(response.data as Map<String, dynamic>);
  }

  Future<List<LivreurOrder>> listOrders({String? status}) async {
    final response = await _dio.get(
      '/livreur/orders',
      queryParameters: {'status': ?status},
    );
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content
        .map((e) => LivreurOrder.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<LivreurOrder> getOrder(int id) async {
    final response = await _dio.get('/livreur/orders/$id');
    return LivreurOrder.fromJson(response.data as Map<String, dynamic>);
  }

  Future<LivreurOrder> markPickedUp(int id) async {
    final response = await _dio.post('/livreur/orders/$id/recuperer');
    return LivreurOrder.fromJson(response.data as Map<String, dynamic>);
  }

  Future<LivreurOrder> markEnRoute(int id) async {
    final response = await _dio.post('/livreur/orders/$id/en-route');
    return LivreurOrder.fromJson(response.data as Map<String, dynamic>);
  }

  Future<LivreurOrder> confirmDelivery(int id, XFile proofPhoto) async {
    final formData = FormData.fromMap({
      'proofType': 'PHOTO',
      'proof': await MultipartFile.fromFile(
        proofPhoto.path,
        filename: 'preuve.jpg',
      ),
    });
    final response = await _dio.post(
      '/livreur/orders/$id/confirmer-remise',
      data: formData,
    );
    return LivreurOrder.fromJson(response.data as Map<String, dynamic>);
  }
}
