import 'package:dio/dio.dart';

import '../auth/token_storage.dart';
import 'api_config.dart';

/// Client HTTP partage par toute l'app. Attache automatiquement le token
/// d'acces, et tente un refresh unique en cas de 401 avant de rejouer la
/// requete d'origine (cf. backend/.../auth/JwtAuthFilter + AuthService.refresh).
class ApiClient {
  ApiClient._internal() {
    _dio = Dio(BaseOptions(baseUrl: ApiConfig.baseUrl));
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _tokenStorage.readAccessToken();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onError: (error, handler) async {
        final isAuthRoute = error.requestOptions.path.contains('/auth/');
        if (error.response?.statusCode == 401 && !isAuthRoute && !_isRetry(error.requestOptions)) {
          final refreshed = await _tryRefresh();
          if (refreshed) {
            final retryOptions = error.requestOptions;
            retryOptions.extra['retried'] = true;
            try {
              final response = await _dio.fetch(retryOptions);
              handler.resolve(response);
              return;
            } catch (_) {
              // tombe dans le handler.next ci-dessous
            }
          }
        }
        handler.next(error);
      },
    ));
  }

  static final ApiClient instance = ApiClient._internal();

  late final Dio _dio;
  final TokenStorage _tokenStorage = TokenStorage();

  Dio get dio => _dio;

  bool _isRetry(RequestOptions options) => options.extra['retried'] == true;

  Future<bool> _tryRefresh() async {
    final refreshToken = await _tokenStorage.readRefreshToken();
    if (refreshToken == null) return false;
    try {
      final response = await _dio.post('/auth/refresh', data: {'refreshToken': refreshToken});
      await _tokenStorage.save(
        accessToken: response.data['accessToken'] as String,
        refreshToken: response.data['refreshToken'] as String,
      );
      return true;
    } catch (_) {
      await _tokenStorage.clear();
      return false;
    }
  }
}
