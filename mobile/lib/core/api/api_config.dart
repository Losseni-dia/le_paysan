/// Base URL de l'API Le Paysan. En dev, pointer vers l'IP LAN de la machine qui
/// fait tourner le backend (pas "localhost" : un emulateur/telephone est un
/// autre peripherique). Ex. http://192.168.1.42:8080/api/v1
///
/// L'emulateur Android peut utiliser 10.0.2.2 pour joindre le localhost de la
/// machine hote (alias special fourni par l'emulateur Android, pas une vraie IP).
class ApiConfig {
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080/api/v1',
  );
}
