import {
  Component, OnInit, AfterViewInit, OnDestroy,
  ViewChild, ElementRef
} from '@angular/core';
import { CommonModule }   from '@angular/common';
import { FormsModule }    from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { MatCardModule }             from '@angular/material/card';
import { MatButtonModule }           from '@angular/material/button';
import { MatIconModule }             from '@angular/material/icon';
import { MatProgressSpinnerModule }  from '@angular/material/progress-spinner';
import { MatChipsModule }            from '@angular/material/chips';
import { MatDividerModule }          from '@angular/material/divider';
import { MatInputModule }            from '@angular/material/input';
import { MatFormFieldModule }        from '@angular/material/form-field';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog }    from '@angular/material/dialog';

import * as L from 'leaflet';
import { ApiService }  from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { Ride }        from '../../../core/models/ride.model';
import {
  ConfirmDialogComponent, ConfirmDialogData
} from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-ride-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, MatChipsModule, MatDividerModule,
    MatInputModule, MatFormFieldModule, MatSnackBarModule, MatDialogModule
  ],
  templateUrl: './ride-detail.component.html',
  styleUrls:   ['./ride-detail.component.scss']
})
export class RideDetailComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('mapEl', { static: false }) mapEl!: ElementRef;

  ride:       Ride | null = null;
  loading     = true;
  isOwner     = false;
  cancelling  = false;
  booking     = false;   // request in-flight flag
  seatsBooked = 1;

  private map?: L.Map;

  constructor(
    private route:  ActivatedRoute,
    private router: Router,
    private api:    ApiService,
    private auth:   AuthService,
    private snack:  MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getRide(id).subscribe({
      next: (r) => {
        this.ride    = r;
        this.loading = false;
        const me     = this.auth.getCurrentSession();
        this.isOwner = me?.userId === r.driver.id;
        setTimeout(() => this.initMap(), 100);
      },
      error: () => {
        this.loading = false;
        this.snack.open('Ride not found.', 'Close', { duration: 3000 });
        this.router.navigate(['/rides/search']);
      }
    });
  }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void { this.map?.remove(); }

  // ── Seat controls ────────────────────────────────────────────────

  decreaseSeats(): void {
    if (this.seatsBooked > 1) this.seatsBooked--;
  }

  increaseSeats(): void {
    const max = this.ride?.seatsAvailable ?? 1;
    if (this.seatsBooked < max && this.seatsBooked < 10) this.seatsBooked++;
  }

  get bookingAmount(): number {
    return (this.ride?.pricePerSeat ?? 0) * this.seatsBooked;
  }

  // ── Book ride ────────────────────────────────────────────────────

  bookRide(): void {
    if (!this.ride || this.booking) return;
    this.booking = true;

    this.api.requestBooking({ rideId: this.ride.id, seatsBooked: this.seatsBooked }).subscribe({
      next: (b) => {
        // B9 fix: navigate once — via the snackbar action only.
        // Removing the immediate router.navigate so the user can see the
        // confirmation snackbar; clicking "View" takes them to the booking.
        this.snack.open('Booking request sent! Tap View to open it.', 'View', { duration: 6000 })
          .onAction().subscribe(() => this.router.navigate(['/bookings', b.id]));
        this.booking = false;
      },
      error: (err) => {
        this.snack.open(err?.error?.error ?? 'Booking failed.', 'Close', { duration: 4000 });
        this.booking = false;
      }
    });
  }

  // ── Cancel ride (driver) ─────────────────────────────────────────

  cancelRide(): void {
    if (!this.ride) return;
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        'Cancel Ride',
        message:      'Cancel this ride? All pending bookings will be rejected and passengers will be notified.',
        confirmLabel: 'Cancel Ride',
        confirmColor: 'warn'
      }
    });
    ref.afterClosed().subscribe(confirmed => { if (confirmed) this._doCancelRide(); });
  }

  private _doCancelRide(): void {
    this.cancelling = true;
    this.api.cancelRide(this.ride!.id).subscribe({
      next: (r) => {
        this.ride = r;
        this.cancelling = false;
        this.snack.open('Ride cancelled.', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.cancelling = false;
        this.snack.open(err.error?.error || 'Failed to cancel.', 'Close', { duration: 3000 });
      }
    });
  }

  // ── Leaflet map ──────────────────────────────────────────────────

  private initMap(): void {
    if (!this.mapEl || !this.ride) return;
    const r = this.ride;

    this.map = L.map(this.mapEl.nativeElement).setView(
      [r.fromLat, r.fromLng], 10
    );

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    L.marker([r.fromLat, r.fromLng])
      .bindPopup(`<strong>From:</strong> ${r.fromLocation}`).addTo(this.map);

    r.stops.forEach((s, i) => {
      L.circleMarker([s.lat, s.lng], {
        radius: 7, color: '#1a237e', fillColor: '#3949ab', fillOpacity: 0.8
      }).bindPopup(`Stop ${i + 1}: ${s.stopName}`).addTo(this.map!);
    });

    L.marker([r.toLat, r.toLng])
      .bindPopup(`<strong>To:</strong> ${r.toLocation}`).addTo(this.map);

    const points: [number, number][] = [
      [r.fromLat, r.fromLng],
      ...r.stops.sort((a, b) => a.sequence - b.sequence).map(s => [s.lat, s.lng] as [number, number]),
      [r.toLat, r.toLng]
    ];
    L.polyline(points, { color: '#1a237e', weight: 3, dashArray: '6, 8' }).addTo(this.map);

    const bounds = L.latLngBounds([[r.fromLat, r.fromLng], [r.toLat, r.toLng]]);
    this.map.fitBounds(bounds, { padding: [40, 40] });
  }
}
