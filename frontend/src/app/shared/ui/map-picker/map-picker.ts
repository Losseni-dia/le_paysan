import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import * as L from 'leaflet';

// Fix du chemin des icônes par défaut de Leaflet (cassé par le bundler Angular).
const DEFAULT_ICON = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

/**
 * Sélecteur de localisation sur carte (OpenStreetMap + Leaflet).
 * Utilisé pour la localisation obligatoire de la boutique (cf. docs/02 UC-02)
 * et l'affichage de la fiche boutique publique.
 */
@Component({
  selector: 'app-map-picker',
  imports: [],
  templateUrl: './map-picker.html',
  styleUrl: './map-picker.scss',
})
export class MapPicker implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  /** Ville saisie par l'utilisateur, utilisée pour centrer la carte par défaut si lat/lng absents. */
  @Input() centerCityFallback: [number, number] = [5.3599, -4.0083]; // Abidjan, par défaut
  @Input() latitude: number | null = null;
  @Input() longitude: number | null = null;
  @Input() readonly = false;

  @Output() positionChange = new EventEmitter<{ latitude: number; longitude: number }>();

  private map?: L.Map;
  private marker?: L.Marker;

  ngAfterViewInit(): void {
    const start: [number, number] =
      this.latitude != null && this.longitude != null
        ? [this.latitude, this.longitude]
        : this.centerCityFallback;

    this.map = L.map(this.mapContainer.nativeElement).setView(start, 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 19,
    }).addTo(this.map);

    this.marker = L.marker(start, { draggable: !this.readonly, icon: DEFAULT_ICON }).addTo(this.map);

    if (!this.readonly) {
      this.marker.on('dragend', () => this.emitPosition());
      this.map.on('click', (e: L.LeafletMouseEvent) => {
        this.marker?.setLatLng(e.latlng);
        this.emitPosition();
      });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.map || !this.marker) {
      return;
    }
    if (changes['latitude'] || changes['longitude']) {
      if (this.latitude != null && this.longitude != null) {
        const pos: [number, number] = [this.latitude, this.longitude];
        this.marker.setLatLng(pos);
        this.map.panTo(pos);
      }
    }
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private emitPosition(): void {
    if (!this.marker) {
      return;
    }
    const { lat, lng } = this.marker.getLatLng();
    this.positionChange.emit({ latitude: lat, longitude: lng });
  }
}
