export interface CiCity {
  name: string;
  lat: number;
  lng: number;
}

/** Grandes villes de Côte d'Ivoire, pour le filtre "ville" du catalogue (tri par distance). */
export const CI_CITIES: CiCity[] = [
  { name: 'Abidjan', lat: 5.3599, lng: -4.0083 },
  { name: 'Yamoussoukro', lat: 6.8276, lng: -5.2893 },
  { name: 'Bouaké', lat: 7.69, lng: -5.03 },
  { name: 'San-Pédro', lat: 4.7485, lng: -6.6363 },
  { name: 'Korhogo', lat: 9.458, lng: -5.6296 },
  { name: 'Daloa', lat: 6.877, lng: -6.4502 },
  { name: 'Man', lat: 7.4125, lng: -7.5538 },
  { name: 'Gagnoa', lat: 6.1319, lng: -5.9506 },
  { name: 'Abengourou', lat: 6.7297, lng: -3.4964 },
  { name: 'Divo', lat: 5.8372, lng: -5.3572 },
  { name: 'Odienné', lat: 9.5096, lng: -7.5664 },
  { name: 'Agboville', lat: 5.9333, lng: -4.2167 },
  { name: 'Grand-Bassam', lat: 5.2106, lng: -3.7384 },
  { name: 'Dabou', lat: 5.3167, lng: -4.3833 },
  { name: 'Bondoukou', lat: 8.0402, lng: -2.8 },
  { name: 'Séguéla', lat: 7.9617, lng: -6.6731 },
  { name: 'Soubré', lat: 5.785, lng: -6.5981 },
  { name: 'Adzopé', lat: 6.1056, lng: -3.8581 },
  { name: 'Issia', lat: 6.49, lng: -6.585 },
  { name: 'Ferkessédougou', lat: 9.6, lng: -5.2 },
];
