import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'order.dart';

class OrderService {
  final Dio _dio = ApiClient.instance.dio;

  /// Transforme le panier en une ou plusieurs commandes (une par boutique), cf.
  /// backend OrderService.checkout(). Vide le panier cote serveur.
  Future<List<Order>> checkout() async {
    final response = await _dio.post('/orders');
    return (response.data as List<dynamic>)
        .map((e) => Order.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<Order> getMine(int id) async {
    final response = await _dio.get('/orders/$id');
    return Order.fromJson(response.data as Map<String, dynamic>);
  }
}
