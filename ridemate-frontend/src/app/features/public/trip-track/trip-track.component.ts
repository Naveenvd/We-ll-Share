import {
  Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit
} from '@angular/core';
import { CommonModule }           from '@angular/common';
import { ActivatedRoute }         from '@angular/router';
import { MatCardModule }          from '@angular/material/card';
import { MatIconModule }          from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import * as L from 'leaflet';
import { ApiService } from '../../../core/services/api.service';
import { Booking }    from '../../../core/models/booking.model';

/**
 * Public trip-share page — accessible without login.
 * URL: /track/:token
 *
 * Shows: booking status, route map, driver & passenger names,
 * and estimated departure time. No sensitive data is exposed.
 */
@Component({
  selector: 'app-trip-track',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule, MatIconModule, MatProgressSpinnerModule
  ],
  templateUrl: './trip-track.component.html',
  styleUrls:   ['./trip-track.component.scss']
})
export class TripTrackComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('mapEl') mapEl!: ElementRef;

  booking: Booking | null = null;
  loading = true;
  error   = false;

  private map?: L.Map;

  constructor(
    private route: ActivatedRoute,
    private api:   ApiService
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token') ?? '';
    this.api.getBookingByShareToken(token).subscribe({
      next: (b) => {
        this.booking = b;
        this.loading  = false;
        setTimeout(() => this.initMap(), 150);
      },
      error: () => {
        this.loading = false;
        this.error   = true;
      }
    });
  }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void { this.map?.remove(); }

  statusLabel(s: string): string { return s.replace('_', ' '); }

  private initMap(): void {
    if (!this.mapEl || !this.booking) return;
    const r = this.booking.ride;

    this.map = L.map(this.mapEl.nativeElement).setView([r.fromLat, r.fromLng], 10);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    L.marker([r.fromLat, r.fromLng])
      .bindPopup(`<strong>From:</strong> ${r.fromLocation}`).addTo(this.map);

    (r.stops ?? []).forEach((s, i) => {
      L.circleMarker([s.lat, s.lng], {
        radius: 7, color: '#1a237e', fillColor: '#3949ab', fillOpacity: 0.8
      }).bindPopup(`Stop ${i + 1}: ${s.stopName}`).addTo(this.map!);
    });

    L.marker([r.toLat, r.toLng])
      .bindPopup(`<strong>To:</strong> ${r.toLocation}`).addTo(this.map);

    const points: [number, number][] = [
      [r.fromLat, r.fromLng],
      ...(r.stops ?? []).sort((a, b) => a.sequence - b.sequence)
        .map(s => [s.lat, s.lng] as [number, number]),
      [r.toLat, r.toLng]
    ];
    L.polyline(points, { color: '#1a237e', weight: 3, dashArray: '6, 8' }).addTo(this.map);
    this.map.fitBounds(
      L.latLngBounds([[r.fromLat, r.fromLng], [r.toLat, r.toLng]]),
      { padding: [40, 40] }
    );
  }
}
