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
import { MatDialogModule, MatDialog }      from '@angular/material/dialog';

import { ApiService }  from '../../../core/services/api.service';
import { Booking }     from '../../../core/models/booking.model';
import {
  ConfirmDialogComponent, ConfirmDialogData
} from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-driver-bookings',
  standalone: true,
  imports: [
    CommonModule, RouterModule, FormsModule,
    MatTabsModule, MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule,
    MatProgressSpinnerModule, MatSnackBarModule, MatDialogModule
  ],
  templateUrl: './driver-bookings.component.html',
  styleUrls:   ['./driver-bookings.component.scss']
})
export class DriverBookingsComponent implements OnInit {

  pending:  Booking[] = [];
  all:      Booking[] = [];
  loading   = true;
  busy: { [id: number]: boolean } = {};

  /** Keyed by booking id — stores the OTP string the driver types */
  otpInputs: { [id: number]: string } = {};

  constructor(
    private api:    ApiService,
    private snack:  MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.api.getDriverBookings().subscribe({
      next: list => {
        this.all     = list;
        this.pending = list.filter(b => b.status === 'PENDING');
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  approve(id: number): void {
    this.busy[id] = true;
    this.api.approveBooking(id).subscribe({
      next: () => { this.snack.open('Booking approved ✓', 'OK', { duration: 3000 }); this.load(); },
      error: err => {
        this.snack.open(err?.error?.error ?? 'Error', 'OK', { duration: 3000 });
        this.busy[id] = false;
      }
    });
  }

  reject(id: number): void {
    this.busy[id] = true;
    this.api.rejectBooking(id).subscribe({
      next: () => { this.snack.open('Booking rejected.', 'OK', { duration: 3000 }); this.load(); },
      error: err => {
        this.snack.open(err?.error?.error ?? 'Error', 'OK', { duration: 3000 });
        this.busy[id] = false;
      }
    });
  }

  startRide(rideId: number): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        'Start Ride',
        message:      'Start this ride? OTPs will be generated for all approved passengers.',
        confirmLabel: 'Start Ride',
        confirmColor: 'primary'
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.api.startRide(rideId).subscribe({
        next: () => { this.snack.open('Ride started! OTPs sent to passengers.', 'OK', { duration: 4000 }); this.load(); },
        error: err => this.snack.open(err?.error?.error ?? 'Error', 'OK', { duration: 3000 })
      });
    });
  }

  verifyOtp(bookingId: number): void {
    const otp = (this.otpInputs[bookingId] ?? '').trim();
    if (otp.length !== 4) {
      this.snack.open('Enter the 4-digit OTP.', 'OK', { duration: 2500 });
      return;
    }
    this.busy[bookingId] = true;
    this.api.verifyTripOtp(bookingId, otp).subscribe({
      next: () => {
        this.snack.open('OTP verified — passenger boarded ✓', 'OK', { duration: 3000 });
        delete this.otpInputs[bookingId];
        this.load();
      },
      error: err => {
        this.snack.open(err?.error?.error ?? 'Invalid OTP', 'OK', { duration: 3000 });
        this.busy[bookingId] = false;
      }
    });
  }

  complete(id: number): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        'Complete Booking',
        message:      'Mark this booking as completed? This finalises the trip for this passenger.',
        confirmLabel: 'Complete',
        confirmColor: 'primary'
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.api.completeBooking(id).subscribe({
        next: () => { this.snack.open('Booking completed ✓', 'OK', { duration: 3000 }); this.load(); },
        error: err => this.snack.open(err?.error?.error ?? 'Error', 'OK', { duration: 3000 })
      });
    });
  }

  statusLabel(s: string): string { return s.replace('_', ' '); }

  /** Find the ride ID for any booking in the "APPROVED" group */
  rideIdForApproved(bookings: Booking[]): number | null {
    const b = bookings.find(x => x.status === 'APPROVED');
    return b ? b.ride.id : null;
  }

  get approvedBookings(): Booking[] {
    return this.all.filter(b => b.status === 'APPROVED');
  }

  get startedBookings(): Booking[] {
    return this.all.filter(b => b.status === 'STARTED');
  }

  get closedBookings(): Booking[] {
    return this.all.filter(b =>
      b.status === 'COMPLETED' || b.status === 'REJECTED' || b.status === 'CANCELLED');
  }
}
