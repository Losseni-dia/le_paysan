import 'package:dio/dio.dart';

import '../api/api_client.dart';

class PaymentService {
  final Dio _dio = ApiClient.instance.dio;

  /// Deep link mobile enregistre dans AndroidManifest.xml (intent-filter) : une fois
  /// le paiement termine, Fedapay redirige le navigateur vers cette URL, ce que le
  /// systeme intercepte pour ramener l'app au premier plan (cf. backend
  /// PaymentService.resolveReturnUrl, qui valide ce schema via app.mobile-deeplink-scheme).
  /// Sans ce parametre, Fedapay retombe sur sa page web de retour par defaut
  /// (ex. http://localhost:4200/...), injoignable depuis le navigateur du mobile.
  static const _mobileReturnUrl = 'lepaysan://paiement-retour';

  Future<String> initiate(List<int> orderIds) async {
    final response =
        await _dio.post('/payments', data: {'orderIds': orderIds, 'returnUrl': _mobileReturnUrl});
    return response.data['paymentUrl'] as String;
  }
}
