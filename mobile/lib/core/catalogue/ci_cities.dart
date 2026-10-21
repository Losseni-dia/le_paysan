class CiCity {
  final String name;
  final double lat;
  final double lng;

  const CiCity({required this.name, required this.lat, required this.lng});
}

/// Grandes villes de Côte d'Ivoire, pour le choix de la ville de livraison
/// (cf. frontend/src/app/core/catalogue/ci-cities.ts - meme liste, cote mobile).
const List<CiCity> ciCities = [
  CiCity(name: 'Abidjan', lat: 5.3599, lng: -4.0083),
  CiCity(name: 'Yamoussoukro', lat: 6.8276, lng: -5.2893),
  CiCity(name: 'Bouaké', lat: 7.69, lng: -5.03),
  CiCity(name: 'San-Pédro', lat: 4.7485, lng: -6.6363),
  CiCity(name: 'Korhogo', lat: 9.458, lng: -5.6296),
  CiCity(name: 'Daloa', lat: 6.877, lng: -6.4502),
  CiCity(name: 'Man', lat: 7.4125, lng: -7.5538),
  CiCity(name: 'Gagnoa', lat: 6.1319, lng: -5.9506),
  CiCity(name: 'Abengourou', lat: 6.7297, lng: -3.4964),
  CiCity(name: 'Divo', lat: 5.8372, lng: -5.3572),
  CiCity(name: 'Odienné', lat: 9.5096, lng: -7.5664),
  CiCity(name: 'Agboville', lat: 5.9333, lng: -4.2167),
  CiCity(name: 'Grand-Bassam', lat: 5.2106, lng: -3.7384),
  CiCity(name: 'Dabou', lat: 5.3167, lng: -4.3833),
  CiCity(name: 'Bondoukou', lat: 8.0402, lng: -2.8),
  CiCity(name: 'Séguéla', lat: 7.9617, lng: -6.6731),
  CiCity(name: 'Soubré', lat: 5.785, lng: -6.5981),
  CiCity(name: 'Adzopé', lat: 6.1056, lng: -3.8581),
  CiCity(name: 'Issia', lat: 6.49, lng: -6.585),
  CiCity(name: 'Ferkessédougou', lat: 9.6, lng: -5.2),
];
