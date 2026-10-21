import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import '../api/api_client.dart';
import 'cart.dart';

/// Etat du panier partage dans toute l'app (badge du catalogue, ecran panier).
class CartService extends ChangeNotifier {
  final Dio _dio = ApiClient.instance.dio;

  Cart? _cart;
  Cart? get cart => _cart;
  int get itemCount => _cart?.items.length ?? 0;

  Future<void> fetch() async {
    final response = await _dio.get('/cart');
    _cart = Cart.fromJson(response.data as Map<String, dynamic>);
    notifyListeners();
  }

  Future<void> addItem({
    required int productId,
    required int quantity,
    required String deliveryMode,
    String? deliveryAddress,
    String? deliveryCityName,
    double? deliveryCityLat,
    double? deliveryCityLng,
  }) async {
    final response = await _dio.post(
      '/cart/items',
      data: {
        'productId': productId,
        'quantity': quantity,
        'deliveryMode': deliveryMode,
        'deliveryAddress': ?deliveryAddress,
        'deliveryCityName': ?deliveryCityName,
        'deliveryCityLat': ?deliveryCityLat,
        'deliveryCityLng': ?deliveryCityLng,
      },
    );
    _cart = Cart.fromJson(response.data as Map<String, dynamic>);
    notifyListeners();
  }

  Future<void> updateItem({
    required int productId,
    required int quantity,
    required String deliveryMode,
    String? deliveryAddress,
    String? deliveryCityName,
    double? deliveryCityLat,
    double? deliveryCityLng,
  }) async {
    final response = await _dio.put(
      '/cart/items/$productId',
      data: {
        'quantity': quantity,
        'deliveryMode': deliveryMode,
        'deliveryAddress': ?deliveryAddress,
        'deliveryCityName': ?deliveryCityName,
        'deliveryCityLat': ?deliveryCityLat,
        'deliveryCityLng': ?deliveryCityLng,
      },
    );
    _cart = Cart.fromJson(response.data as Map<String, dynamic>);
    notifyListeners();
  }

  Future<void> removeItem(int productId) async {
    final response = await _dio.delete('/cart/items/$productId');
    _cart = Cart.fromJson(response.data as Map<String, dynamic>);
    notifyListeners();
  }

  void clear() {
    _cart = null;
    notifyListeners();
  }
}
