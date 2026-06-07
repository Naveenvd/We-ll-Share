import { Component, OnInit }              from '@angular/core';
import { CommonModule }                    from '@angular/common';
import { RouterModule }                    from '@angular/router';
import { FormsModule }                     from '@angular/forms';
import { MatTabsModule }                   from '@angular/material/tabs';
import { MatCardModule }                   from '@angular/material/card';
import { MatButtonModule }                 from '@angular/material/button';
import { MatIconModule }                   from '@angular/material/icon';
import { MatInputModule }                  from '@angular/material/input';
import { MatFormFieldModule }              from '@angular/material/form-field';
import { MatProgressSpinnerModule }        from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule }  from '@angular/material/snack-bar';

import { ApiService }  from '../../../core/services/api.service';
import { Parcel }      from '../../../core/models/parcel.model';

@Component({
  selector: 'app-driver-parcels',
  standalone: true,
  imports: [
    CommonModule, RouterModule, FormsModule,
    MatTabsModule, MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule,
    MatProgressSpinnerModule, MatSnackBarModule
  ],
  templateUrl: './driver-parcels.component.html',
  styleUrls:   ['./driver-parcels.component.scss']
})
export class DriverParcelsComponent implements OnInit {

  all:     Parcel[] = [];
  loading  = true;
  busy: { [id: number]: boolean } = {};

  /** OTP inputs keyed by parcel id */
  otpInputs: { [id: number]: string } = {};

  /** Selected before/after photos keyed by parcel id */
  beforePhotos: { [id: number]: File | null } = {};
  afterPhotos:  { [id: number]: File | null } = {};

  constructor(private api: ApiService, private snack: MatSnackBar) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.api.getDriverParcels().subscribe({
      next: list => { this.all = list; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get pending():   Parcel[] { return this.all.filter(p => p.status === 'PENDING'); }
  get accepted():  Parcel[] { return this.all.filter(p => p.status === 'ACCEPTED'); }
  get inTransit(): Parcel[] { return this.all.filter(p => p.status === 'IN_TRANSIT'); }
  get closed():    Parcel[] { return this.all.filter(p =>
    ['DELIVERED', 'REJECTED', 'CANCELLED', 'COMPLAINT_RAISED'].includes(p.status)); }

  accept(id: number): void {
    this.busy[id] = true;
    this.api.acceptParcel(id).subscribe({
      next: () => { this.snack.open('Parcel accepted ✓', 'OK', { duration: 3000 }); this.load(); },
      error: err => { this.snack.open(err?.error?.error ?? 'Error', 'OK', { duration: 3000 }); this.busy[id] = false; }
    });
  }

  reject(id: number): void {
    this.busy[id] = true;
    this.api.rejectParcel(id).subscribe({
      next: () => { this.snack.open('Parcel rejected.', 'OK', { duration: 3000 }); this.load(); },
      error: err => { this.snack.open(err?.error?.error ?? 'Error', 'OK', { duration: 3000 }); this.busy[id] = false; }
    });
  }

  onBeforePhoto(event: Event, id: number): void {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.beforePhotos[id] = file;
  }

  onAfterPhoto(event: Event, id: number): void {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.afterPhotos[id] = file;
  }

  verifyPickup(id: number): void {
    const otp = (this.otpInputs[id] ?? '').trim();
    if (otp.length !== 4) { this.snack.open('Enter the 4-digit pickup OTP.', 'OK', { duration: 2500 }); return; }
    this.busy[id] = true;
    const photo = this.beforePhotos[id] ?? undefined;
    this.api.verifyPickupOtp(id, otp, photo).subscribe({
      next: () => {
        this.snack.open('Pickup verified — parcel in transit ✓', 'OK', { duration: 3000 });
        delete this.otpInputs[id];
        delete this.beforePhotos[id];
        this.load();
      },
      error: err => { this.snack.open(err?.error?.error ?? 'Invalid OTP', 'OK', { duration: 3000 }); this.busy[id] = false; }
    });
  }

  verifyDelivery(id: number): void {
    const otp = (this.otpInputs[id] ?? '').trim();
    if (otp.length !== 4) { this.snack.open('Enter the 4-digit delivery OTP.', 'OK', { duration: 2500 }); return; }
    this.busy[id] = true;
    const photo = this.afterPhotos[id] ?? undefined;
    this.api.verifyDeliveryOtp(id, otp, photo).subscribe({
      next: () => {
        this.snack.open('Delivery confirmed ✓', 'OK', { duration: 3000 });
        delete this.otpInputs[id];
        delete this.afterPhotos[id];
        this.load();
      },
      error: err => { this.snack.open(err?.error?.error ?? 'Invalid OTP', 'OK', { duration: 3000 }); this.busy[id] = false; }
    });
  }

  statusLabel(s: string): string { return s.replace('_', ' '); }
}
