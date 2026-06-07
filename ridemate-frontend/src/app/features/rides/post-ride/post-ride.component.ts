import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { MatDividerModule } from '@angular/material/divider';

import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { MapPickerComponent } from '../../../shared/components/map-picker/map-picker.component';
import { LatLng } from '../../../core/models/ride.model';
import { Vehicle } from '../../../core/models/profile.model';

@Component({
  selector: 'app-post-ride',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatIconModule, MatSelectModule, MatDatepickerModule, MatNativeDateModule,
    MatSlideToggleModule, MatProgressSpinnerModule, MatSnackBarModule,
    MatStepperModule, MatDividerModule, MapPickerComponent
  ],
  templateUrl: './post-ride.component.html',
  styleUrls:   ['./post-ride.component.scss']
})
export class PostRideComponent implements OnInit {

  vehicles: Vehicle[] = [];
  loading = false;
  errorMessage = '';
  isFemaleDriver = false;
  minDeparture = new Date();

  fromLocation: LatLng | null = null;
  toLocation:   LatLng | null = null;

  /** Straight-line distance estimate shown to driver once both pins are placed */
  get estimatedKm(): number | null {
    if (!this.fromLocation || !this.toLocation) return null;
    const R = 6371;
    const dLat = this.deg2rad(this.toLocation.lat - this.fromLocation.lat);
    const dLng = this.deg2rad(this.toLocation.lng - this.fromLocation.lng);
    const a = Math.sin(dLat / 2) ** 2
      + Math.cos(this.deg2rad(this.fromLocation.lat))
      * Math.cos(this.deg2rad(this.toLocation.lat))
      * Math.sin(dLng / 2) ** 2;
    return Math.round(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
  }

  private deg2rad(d: number): number { return d * (Math.PI / 180); }

  // ── Step 1: Route ──────────────────────────────────────────────
  routeForm = this.fb.group({
    fromAddress: ['', Validators.required],
    toAddress:   ['', Validators.required]
  });

  // ── Step 2: Details ────────────────────────────────────────────
  detailsForm = this.fb.group({
    departureDate:     [null as Date | null, Validators.required],
    departureTime:     ['', [Validators.required, Validators.pattern(/^\d{2}:\d{2}$/)]],
    seatsTotal:        [1,  [Validators.required, Validators.min(1), Validators.max(10)]],
    pricePerSeat:      ['', [Validators.required, Validators.min(1)]],
    vehicleId:         ['', Validators.required],
    acceptsPassengers: [true],
    acceptsParcels:    [false],
    maxParcelSize:     ['SMALL'],
    womenOnly:         [false]
  });

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private auth: AuthService,
    private router: Router,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.api.getVehicles().subscribe(v => this.vehicles = v);
    this.api.getProfile().subscribe(p => {
      this.isFemaleDriver = p.gender === 'FEMALE';
    });
  }

  onFromPicked(loc: LatLng): void {
    this.fromLocation = loc;
    this.routeForm.get('fromAddress')!.setValue(loc.address ?? '');
  }

  onToPicked(loc: LatLng): void {
    this.toLocation = loc;
    this.routeForm.get('toAddress')!.setValue(loc.address ?? '');
  }

  submit(): void {
    if (!this.fromLocation || !this.toLocation) {
      this.errorMessage = 'Please select both pickup and drop locations on the map.';
      return;
    }
    if (this.detailsForm.invalid) return;

    const d = this.detailsForm.value;
    const dDate = d.departureDate as Date;
    const [hh, mm] = (d.departureTime as string).split(':').map(Number);
    dDate.setHours(hh, mm, 0, 0);

    if (!d.acceptsPassengers && !d.acceptsParcels) {
      this.errorMessage = 'Ride must accept passengers, parcels, or both.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.api.postRide({
      fromLocation:      this.fromLocation.address ?? this.routeForm.value.fromAddress!,
      toLocation:        this.toLocation.address   ?? this.routeForm.value.toAddress!,
      fromLat: this.fromLocation.lat, fromLng: this.fromLocation.lng,
      toLat:   this.toLocation.lat,   toLng:   this.toLocation.lng,
      departureTime:     dDate.toISOString().slice(0, 19),
      seatsTotal:        Number(d.seatsTotal),
      pricePerSeat:      Number(d.pricePerSeat),
      acceptsPassengers: !!d.acceptsPassengers,
      acceptsParcels:    !!d.acceptsParcels,
      maxParcelSize:     d.acceptsParcels ? (d.maxParcelSize as any) : undefined,
      womenOnly:         !!d.womenOnly,
      vehicleId:         Number(d.vehicleId),
      stops:             []   // route auto-derived from from/to coordinates by the map
    }).subscribe({
      next: (ride) => {
        this.loading = false;
        this.snack.open('Ride posted successfully!', 'OK', { duration: 3000 });
        this.router.navigate(['/rides', ride.id]);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Failed to post ride. Please try again.';
      }
    });
  }
}
