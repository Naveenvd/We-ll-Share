import {
  Component, OnInit, OnDestroy, Input, Output,
  EventEmitter, AfterViewInit, ElementRef, ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import * as L from 'leaflet';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { NominatimService } from '../../../core/services/nominatim.service';
import { LatLng, NominatimResult } from '../../../core/models/ride.model';

// Fix Leaflet's broken default icon when bundled with Webpack/Angular CLI
const iconDefault = L.icon({
  iconUrl:       'assets/leaflet/marker-icon.png',
  iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
  shadowUrl:     'assets/leaflet/marker-shadow.png',
  iconSize:    [25, 41],
  iconAnchor:  [12, 41],
  popupAnchor: [1, -34],
  shadowSize:  [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-map-picker',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatIconModule,
    MatButtonModule, MatListModule, MatProgressSpinnerModule
  ],
  templateUrl: './map-picker.component.html',
  styleUrls:   ['./map-picker.component.scss']
})
export class MapPickerComponent implements OnInit, AfterViewInit, OnDestroy {

  /** Label shown above the search box */
  @Input() label = 'Pick location';
  /** Initial coordinates (if editing an existing ride) */
  @Input() initialLat?: number;
  @Input() initialLng?: number;
  @Input() initialAddress?: string;
  /** Marker color accent: 'blue' (from) | 'red' (to) */
  @Input() markerColor: 'blue' | 'red' = 'blue';
  /** Show a "Use current location" GPS button above the search field */
  @Input() showCurrentLocation = false;

  /** Emits whenever the user picks a point */
  @Output() locationPicked = new EventEmitter<LatLng>();

  @ViewChild('mapContainer', { static: false }) mapContainer!: ElementRef;

  private map!: L.Map;
  private marker?: L.Marker;
  private destroy$ = new Subject<void>();

  searchCtrl = new FormControl('');
  suggestions: NominatimResult[] = [];
  searching = false;
  showSuggestions = false;

  // GPS state
  locatingUser = false;
  geoError = '';

  // Default map center — India
  private defaultLat = 20.5937;
  private defaultLng = 78.9629;
  private defaultZoom = 5;

  constructor(private nominatim: NominatimService) {}

  ngOnInit(): void {
    // Seed the search box if editing
    if (this.initialAddress) this.searchCtrl.setValue(this.initialAddress, { emitEvent: false });

    // Live autocomplete with 400 ms debounce
    this.searchCtrl.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(val => this.doSearch(val ?? ''));
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.map) this.map.remove();
  }

  // ── Map init ─────────────────────────────────────────────────

  private initMap(): void {
    const lat  = this.initialLat  ?? this.defaultLat;
    const lng  = this.initialLng  ?? this.defaultLng;
    const zoom = this.initialLat  ? 13 : this.defaultZoom;

    this.map = L.map(this.mapContainer.nativeElement, {
      center: [lat, lng],
      zoom,
      zoomControl: true
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://openstreetmap.org">OpenStreetMap</a>',
      maxZoom: 19
    }).addTo(this.map);

    // Place initial marker if coordinates provided
    if (this.initialLat && this.initialLng) {
      this.placeMarker(this.initialLat, this.initialLng);
    }

    // Click on map to pick location
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.placeMarkerAndGeocode(e.latlng.lat, e.latlng.lng);
    });
  }

  // ── Geocoding ─────────────────────────────────────────────────

  private doSearch(query: string): void {
    if (query.length < 3) { this.suggestions = []; return; }
    this.searching = true;
    this.nominatim.search(query).subscribe(results => {
      this.suggestions = results;
      this.showSuggestions = results.length > 0;
      this.searching = false;
    });
  }

  selectSuggestion(result: NominatimResult): void {
    const lat = parseFloat(result.lat);
    const lng = parseFloat(result.lon);
    this.searchCtrl.setValue(result.display_name, { emitEvent: false });
    this.suggestions = [];
    this.showSuggestions = false;
    this.placeMarker(lat, lng);
    this.map.flyTo([lat, lng], 14);
    this.locationPicked.emit({ lat, lng, address: result.display_name });
  }

  private placeMarkerAndGeocode(lat: number, lng: number): void {
    this.placeMarker(lat, lng);
    this.nominatim.reverse(lat, lng).subscribe(address => {
      this.searchCtrl.setValue(address, { emitEvent: false });
      this.locationPicked.emit({ lat, lng, address });
    });
  }

  private placeMarker(lat: number, lng: number): void {
    if (this.marker) this.map.removeLayer(this.marker);
    this.marker = L.marker([lat, lng], { draggable: true }).addTo(this.map);
    this.marker.on('dragend', () => {
      const pos = this.marker!.getLatLng();
      this.placeMarkerAndGeocode(pos.lat, pos.lng);
    });
  }

  hideSuggestions(): void {
    setTimeout(() => { this.showSuggestions = false; }, 200);
  }

  // ── Current Location ─────────────────────────────────────────

  useCurrentLocation(): void {
    if (!navigator.geolocation) {
      this.geoError = 'Geolocation is not supported by your browser.';
      return;
    }
    this.locatingUser = true;
    this.geoError = '';

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        this.placeMarker(lat, lng);
        this.map.flyTo([lat, lng], 15);
        this.nominatim.reverse(lat, lng).subscribe(address => {
          this.searchCtrl.setValue(address, { emitEvent: false });
          this.locationPicked.emit({ lat, lng, address });
          this.locatingUser = false;
        });
      },
      (err) => {
        this.locatingUser = false;
        switch (err.code) {
          case err.PERMISSION_DENIED:
            this.geoError = 'Location permission denied. Allow access in your browser settings.';
            break;
          case err.POSITION_UNAVAILABLE:
            this.geoError = 'Location unavailable. Please search manually.';
            break;
          default:
            this.geoError = 'Could not get location. Please search manually.';
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    );
  }
}
