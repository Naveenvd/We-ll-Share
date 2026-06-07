import { Component, AfterViewInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormControl, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';

import * as L from 'leaflet';
import { ApiService } from '../../../core/services/api.service';
import { NominatimService } from '../../../core/services/nominatim.service';
import { LatLng, Ride, NominatimResult } from '../../../core/models/ride.model';

@Component({
  selector: 'app-search-rides',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatFormFieldModule, MatInputModule, MatButtonModule,
    MatIconModule, MatDatepickerModule, MatNativeDateModule,
    MatSlideToggleModule, MatProgressSpinnerModule, MatSnackBarModule,
    MatPaginatorModule, MatChipsModule
  ],
  templateUrl: './search-rides.component.html',
  styleUrls:   ['./search-rides.component.scss']
})
export class SearchRidesComponent implements AfterViewInit, OnDestroy {

  @ViewChild('routeMap', { static: false }) routeMapEl!: ElementRef;

  // Location state
  fromLocation: LatLng | null = null;
  toLocation:   LatLng | null = null;
  minDate = new Date();

  // Autocomplete controls
  fromCtrl = new FormControl('');
  toCtrl   = new FormControl('');
  fromSuggestions: NominatimResult[] = [];
  toSuggestions:   NominatimResult[] = [];
  fromSearching = false;
  toSearching   = false;
  showFromSugs  = false;
  showToSugs    = false;

  // Which location the next map-click will set
  mapClickMode: 'from' | 'to' = 'from';

  // GPS state
  locatingUser = false;
  geoError = '';

  // Search filters form
  searchForm = this.fb.group({
    date:      [null as Date | null, Validators.required],
    seats:     [1, [Validators.min(1), Validators.max(10)]],
    womenOnly: [false]
  });

  // Results
  rides: Ride[] = [];
  totalElements = 0;
  pageSize = 10;
  loading  = false;
  searched = false;
  errorMessage = '';

  // Leaflet
  private routeMap?: L.Map;
  private fromMarker?: L.Marker;
  private toMarker?:   L.Marker;
  private routeLine?:  L.Polyline;
  private rideMarkers: L.Marker[] = [];

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private nominatim: NominatimService,
    private router: Router
  ) {}

  ngAfterViewInit(): void {
    // Autocomplete listeners
    this.fromCtrl.valueChanges.pipe(
      debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$)
    ).subscribe(v => this.doFromSearch(v ?? ''));

    this.toCtrl.valueChanges.pipe(
      debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$)
    ).subscribe(v => this.doToSearch(v ?? ''));

    // Init map after DOM is painted
    setTimeout(() => this.initRouteMap(), 80);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.routeMap?.remove();
  }

  // ── Autocomplete ─────────────────────────────────────────────

  private doFromSearch(q: string): void {
    if (q.length < 3) { this.fromSuggestions = []; return; }
    this.fromSearching = true;
    this.nominatim.search(q).subscribe(r => {
      this.fromSuggestions = r; this.showFromSugs = r.length > 0; this.fromSearching = false;
    });
  }

  private doToSearch(q: string): void {
    if (q.length < 3) { this.toSuggestions = []; return; }
    this.toSearching = true;
    this.nominatim.search(q).subscribe(r => {
      this.toSuggestions = r; this.showToSugs = r.length > 0; this.toSearching = false;
    });
  }

  selectFrom(r: NominatimResult): void {
    const lat = parseFloat(r.lat), lng = parseFloat(r.lon);
    this.fromCtrl.setValue(r.display_name, { emitEvent: false });
    this.fromSuggestions = []; this.showFromSugs = false;
    this.fromLocation = { lat, lng, address: r.display_name };
    this.placeFromMarker(lat, lng);
    this.mapClickMode = 'to';
  }

  selectTo(r: NominatimResult): void {
    const lat = parseFloat(r.lat), lng = parseFloat(r.lon);
    this.toCtrl.setValue(r.display_name, { emitEvent: false });
    this.toSuggestions = []; this.showToSugs = false;
    this.toLocation = { lat, lng, address: r.display_name };
    this.placeToMarker(lat, lng);
    this.mapClickMode = 'from';
  }

  hideFromSugs(): void { setTimeout(() => this.showFromSugs = false, 200); }
  hideToSugs():   void { setTimeout(() => this.showToSugs   = false, 200); }

  // ── Current Location ─────────────────────────────────────────

  useCurrentLocation(): void {
    if (!navigator.geolocation) {
      this.geoError = 'Geolocation is not supported by your browser.';
      return;
    }
    this.locatingUser = true;
    this.geoError = '';

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        this.nominatim.reverse(lat, lng).subscribe(addr => {
          this.fromCtrl.setValue(addr, { emitEvent: false });
          this.fromLocation = { lat, lng, address: addr };
          this.placeFromMarker(lat, lng);
          this.mapClickMode = 'to';
          this.locatingUser = false;
        });
      },
      (err) => {
        this.locatingUser = false;
        switch (err.code) {
          case err.PERMISSION_DENIED:
            this.geoError = 'Location permission denied. Please allow access in your browser settings.';
            break;
          case err.POSITION_UNAVAILABLE:
            this.geoError = 'Your location could not be determined. Try searching manually.';
            break;
          default:
            this.geoError = 'Could not get your location. Please search manually.';
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    );
  }

  // ── Map ───────────────────────────────────────────────────────

  private initRouteMap(): void {
    if (!this.routeMapEl) return;

    this.routeMap = L.map(this.routeMapEl.nativeElement, {
      center: [20.5937, 78.9629], zoom: 5
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap', maxZoom: 19
    }).addTo(this.routeMap);

    // Map click → place from then to
    this.routeMap.on('click', (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng;
      if (this.mapClickMode === 'from') {
        this.placeFromMarker(lat, lng);
        this.nominatim.reverse(lat, lng).subscribe(addr => {
          this.fromCtrl.setValue(addr, { emitEvent: false });
          this.fromLocation = { lat, lng, address: addr };
          this.mapClickMode = 'to';
          this.updateRouteLine();
        });
      } else {
        this.placeToMarker(lat, lng);
        this.nominatim.reverse(lat, lng).subscribe(addr => {
          this.toCtrl.setValue(addr, { emitEvent: false });
          this.toLocation = { lat, lng, address: addr };
          this.mapClickMode = 'from';
          this.updateRouteLine();
        });
      }
    });
  }

  private fromIcon(): L.DivIcon {
    return L.divIcon({ className: '', html: '<div class="map-pin map-pin--from">A</div>', iconAnchor: [24, 16] });
  }
  private toIcon(): L.DivIcon {
    return L.divIcon({ className: '', html: '<div class="map-pin map-pin--to">B</div>', iconAnchor: [24, 16] });
  }

  private placeFromMarker(lat: number, lng: number): void {
    if (!this.routeMap) return;
    if (this.fromMarker) this.routeMap.removeLayer(this.fromMarker);
    this.fromMarker = L.marker([lat, lng], { icon: this.fromIcon(), draggable: true }).addTo(this.routeMap);
    this.fromMarker.on('dragend', () => {
      const pos = this.fromMarker!.getLatLng();
      this.nominatim.reverse(pos.lat, pos.lng).subscribe(addr => {
        this.fromCtrl.setValue(addr, { emitEvent: false });
        this.fromLocation = { lat: pos.lat, lng: pos.lng, address: addr };
        this.updateRouteLine();
      });
    });
    if (!this.toLocation) this.routeMap.setView([lat, lng], 13);
    this.updateRouteLine();
  }

  private placeToMarker(lat: number, lng: number): void {
    if (!this.routeMap) return;
    if (this.toMarker) this.routeMap.removeLayer(this.toMarker);
    this.toMarker = L.marker([lat, lng], { icon: this.toIcon(), draggable: true }).addTo(this.routeMap);
    this.toMarker.on('dragend', () => {
      const pos = this.toMarker!.getLatLng();
      this.nominatim.reverse(pos.lat, pos.lng).subscribe(addr => {
        this.toCtrl.setValue(addr, { emitEvent: false });
        this.toLocation = { lat: pos.lat, lng: pos.lng, address: addr };
        this.updateRouteLine();
      });
    });
    this.updateRouteLine();
  }

  private updateRouteLine(): void {
    if (!this.routeMap) return;
    if (this.routeLine) { this.routeMap.removeLayer(this.routeLine); this.routeLine = undefined; }
    if (this.fromLocation && this.toLocation) {
      this.routeLine = L.polyline([
        [this.fromLocation.lat, this.fromLocation.lng],
        [this.toLocation.lat,   this.toLocation.lng]
      ], { color: '#7c3aed', weight: 3, opacity: 0.75, dashArray: '10 7' }).addTo(this.routeMap);
      this.routeMap.fitBounds(L.latLngBounds(
        [this.fromLocation.lat, this.fromLocation.lng],
        [this.toLocation.lat,   this.toLocation.lng]
      ), { padding: [60, 60] });
    }
  }

  private addRideMarkersToMap(): void {
    if (!this.routeMap) return;
    this.rideMarkers.forEach(m => this.routeMap!.removeLayer(m));
    this.rideMarkers = [];
    this.rides.forEach(ride => {
      const icon = L.divIcon({ className: '', html: `<div class="map-pin map-pin--ride">₹${ride.pricePerSeat}</div>` });
      const m = L.marker([ride.fromLat, ride.fromLng], { icon })
        .bindPopup(`<strong>${ride.driver.name}</strong><br/>${ride.fromLocation} → ${ride.toLocation}<br/>₹${ride.pricePerSeat}/seat · ${ride.seatsAvailable} left`)
        .addTo(this.routeMap!);
      m.on('click', () => this.viewRide(ride.id));
      this.rideMarkers.push(m);
    });
    setTimeout(() => this.routeMap?.invalidateSize(), 100);
  }

  // ── Search ────────────────────────────────────────────────────

  search(page = 0): void {
    if (!this.fromLocation || !this.toLocation) {
      this.errorMessage = 'Please select both pickup and drop locations.';
      return;
    }
    if (this.searchForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';
    this.searched = true;

    const d    = this.searchForm.value;
    const date = d.date as Date;
    date.setHours(0, 0, 0, 0);

    this.api.searchRides({
      fromLat: this.fromLocation.lat, fromLng: this.fromLocation.lng,
      toLat:   this.toLocation.lat,   toLng:   this.toLocation.lng,
      date:    date.toISOString().slice(0, 19),
      seats:     d.seats ?? 1,
      womenOnly: d.womenOnly ?? undefined,
      page, size: this.pageSize
    }).subscribe({
      next: res => {
        this.rides = res.content;
        this.totalElements = res.totalElements;
        this.loading = false;
        this.addRideMarkersToMap();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Search failed. Please try again.';
      }
    });
  }

  onPage(e: PageEvent): void { this.search(e.pageIndex); }
  viewRide(id: number): void { this.router.navigate(['/rides', id]); }
}
