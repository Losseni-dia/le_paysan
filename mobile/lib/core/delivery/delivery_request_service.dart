import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'delivery_request.dart';
import 'eligible_livreur.dart';

class DeliveryRequestService {
  final Dio _dio = ApiClient.instance.dio;

  /// Livreurs disponibles a proximite de la livraison de cette commande (cote vendeur).
  Future<List<EligibleLivreur>> listEligibleLivreurs(int orderId) async {
    final response = await _dio.get('/vendor/orders/$orderId/livreurs-eligibles');
    return (response.data as List<dynamic>)
        .map((e) => EligibleLivreur.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  /// Diffuse la demande de livraison a plusieurs livreurs : le premier a accepter l'emporte.
  Future<void> broadcast(int orderId, List<int> livreurIds) async {
    await _dio.post(
      '/vendor/orders/$orderId/diffuser',
      data: {'livreurIds': livreurIds},
    );
  }

  /// Demandes en attente pour le livreur connecte.
  Future<List<DeliveryRequest>> listForLivreur() async {
    final response = await _dio.get('/livreur/demandes');
    return (response.data as List<dynamic>)
        .map((e) => DeliveryRequest.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<DeliveryRequest> accept(int id) async {
    final response = await _dio.post('/livreur/demandes/$id/accepter');
    return DeliveryRequest.fromJson(response.data as Map<String, dynamic>);
  }
}
