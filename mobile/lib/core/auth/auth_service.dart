import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

import '../api/api_client.dart';
import 'token_storage.dart';
import 'user.dart';

/// Etat d'authentification partage dans toute l'app (ChangeNotifier ecoute
/// par les widgets via provider, equivalent des signals du AuthService Angular).
class AuthService extends ChangeNotifier {
  final Dio _dio = ApiClient.instance.dio;
  final TokenStorage _tokenStorage = TokenStorage();

  AppUser? _currentUser;
  AppUser? get currentUser => _currentUser;
  bool get isAuthenticated => _currentUser != null;

  bool hasRole(String role) => _currentUser?.hasRole(role) ?? false;

  /// A appeler au demarrage de l'app : restaure la session si un token est present.
  Future<void> tryRestoreSession() async {
    final token = await _tokenStorage.readAccessToken();
    if (token == null) return;
    // Pas d'endpoint "/me" dedie cote backend actuellement : on attend le
    // premier appel authentifie (ex. panier) pour confirmer la validite du
    // token ; sinon le refresh automatique de l'ApiClient prend le relai.
  }

  Future<void> login(String username, String password) async {
    final response = await _dio.post('/auth/login', data: {
      'username': username,
      'password': password,
    });
    await _applyAuthResponse(response.data as Map<String, dynamic>);
  }

  Future<void> register({
    required String username,
    required String password,
    required String firstName,
    required String lastName,
    String? email,
    String? phone,
    required bool acceptCgu,
    required bool acceptCgv,
    required bool acceptPrivacy,
  }) async {
    final response = await _dio.post('/auth/register', data: {
      'username': username,
      'password': password,
      'firstName': firstName,
      'lastName': lastName,
      if (email != null && email.isNotEmpty) 'email': email,
      if (phone != null && phone.isNotEmpty) 'phone': phone,
      'acceptCgu': acceptCgu,
      'acceptCgv': acceptCgv,
      'acceptPrivacy': acceptPrivacy,
    });
    await _applyAuthResponse(response.data as Map<String, dynamic>);
  }

  Future<void> logout() async {
    await _tokenStorage.clear();
    _currentUser = null;
    notifyListeners();
  }

  Future<void> _applyAuthResponse(Map<String, dynamic> data) async {
    await _tokenStorage.save(
      accessToken: data['accessToken'] as String,
      refreshToken: data['refreshToken'] as String,
    );
    _currentUser = AppUser.fromJson(data['user'] as Map<String, dynamic>);
    notifyListeners();
  }
}
