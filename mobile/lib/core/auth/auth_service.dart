import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:image_picker/image_picker.dart';

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
    try {
      final response = await _dio.get('/users/me');
      _currentUser = AppUser.fromJson(response.data as Map<String, dynamic>);
      notifyListeners();
    } catch (_) {
      // Token invalide/expire : le refresh automatique de l'ApiClient prendra
      // le relai au prochain appel authentifie, sinon l'utilisateur devra se reconnecter.
    }
  }

  Future<void> login(String username, String password) async {
    final response = await _dio.post(
      '/auth/login',
      data: {'username': username, 'password': password},
    );
    await _applyAuthResponse(response.data as Map<String, dynamic>);
  }

  Future<void> register({
    required String username,
    required String password,
    required String firstName,
    required String lastName,
    String? email,
    String? phone,
    String? city,
    required bool acceptCgu,
    required bool acceptCgv,
    required bool acceptPrivacy,
  }) async {
    final response = await _dio.post(
      '/auth/register',
      data: {
        'username': username,
        'password': password,
        'firstName': firstName,
        'lastName': lastName,
        if (email != null && email.isNotEmpty) 'email': email,
        if (phone != null && phone.isNotEmpty) 'phone': phone,
        if (city != null && city.isNotEmpty) 'city': city,
        'acceptCgu': acceptCgu,
        'acceptCgv': acceptCgv,
        'acceptPrivacy': acceptPrivacy,
      },
    );
    await _applyAuthResponse(response.data as Map<String, dynamic>);
  }

  /// Flux email classique (cf. docs UC-01) : lien de reinitialisation envoye par email.
  /// Reserve aux comptes ayant un email renseigne - cf. [accountRecoveryRequest] sinon.
  Future<void> forgotPassword(String email) async {
    await _dio.post('/auth/forgot-password', data: {'email': email});
  }

  /// Flux sans email (cf. docs UC-01b) : la majorite des utilisateurs en Cote d'Ivoire
  /// n'ont pas d'adresse email. Le compte est recupere par comparaison manuelle, par un
  /// admin, entre ce selfie fraichement pris et le selfie de securite enregistre a
  /// l'inscription. Resultat : un mot de passe temporaire communique hors-ligne (pas via
  /// cette app, cf. AccountRecoveryController.approve cote backend).
  Future<void> accountRecoveryRequest(String username, XFile selfie) async {
    final formData = FormData.fromMap({
      'username': username,
      'selfie': await MultipartFile.fromFile(
        selfie.path,
        filename: 'selfie.jpg',
      ),
    });
    await _dio.post('/auth/account-recovery-requests', data: formData);
  }

  /// Photo de reference utilisee plus tard pour comparer au selfie soumis lors d'une
  /// demande de recuperation de compte sans email (cf. [accountRecoveryRequest]).
  /// A proposer juste apres l'inscription si l'utilisateur n'a pas renseigne d'email.
  Future<void> updateSecuritySelfie(XFile selfie) async {
    final formData = FormData.fromMap({
      'selfie': await MultipartFile.fromFile(
        selfie.path,
        filename: 'selfie.jpg',
      ),
    });
    final response = await _dio.put(
      '/users/me/security-selfie',
      data: formData,
    );
    _currentUser = AppUser.fromJson(response.data as Map<String, dynamic>);
    notifyListeners();
  }

  Future<void> updateProfile({
    required String firstName,
    required String lastName,
    String? email,
    String? phone,
  }) async {
    final response = await _dio.put(
      '/users/me',
      data: {
        'firstName': firstName,
        'lastName': lastName,
        if (email != null && email.isNotEmpty) 'email': email,
        if (phone != null && phone.isNotEmpty) 'phone': phone,
      },
    );
    _currentUser = AppUser.fromJson(response.data as Map<String, dynamic>);
    notifyListeners();
  }

  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    await _dio.put(
      '/users/me/password',
      data: {'currentPassword': currentPassword, 'newPassword': newPassword},
    );
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
