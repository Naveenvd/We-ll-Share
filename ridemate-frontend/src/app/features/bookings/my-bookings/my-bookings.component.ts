import { Component, OnInit } from '@angular/core';
import { CommonModule }      from '@angular/common';
import { RouterModule }      from '@angular/router';
import { MatCardModule }     from '@angular/material/card';
import { MatButtonModule }   from '@angular/material/button';
import { MatIconModule }     from '@angular/material/icon';
import { MatChipsModule }    from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog }    from '@angular/material/dialog';

import { ApiService }  from '../../../core/services/api.service';
import { Booking }     from '../../../core/models/booking.model';
import {
  ConfirmDialogComponent, ConfirmDialogData
} from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatChipsModule, MatProgressSpinnerModule, MatSnackBarModule, MatDialogModule
  ],
  templateUrl: './my-bookings.component.html',
  styleUrls:   ['./my-bookings.component.scss']
})
export class MyBookingsComponent implements OnInit {

  bookings: Booking[] = [];
  loading  = true;
  cancelling: number | null = null;

  constructor(
    private api:    ApiService,
    private snack:  MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.api.getMyBookings().subscribe({
      next:  b  => { this.bookings = b; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  cancel(id: number): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        'Cancel Booking',
        message:      'Cancel this booking? This action cannot be undone.',
        confirmLabel: 'Cancel Booking',
        confirmColor: 'warn'
      }
    });
    ref.afterClosed().subscribe(confirmed => { if (confirmed) this._doCancel(id); });
  }

  private _doCancel(id: number): void {
    this.cancelling = id;
    this.api.cancelBooking(id).subscribe({
      next: updated => {
        const idx = this.bookings.findIndex(b => b.id === id);
        if (idx !== -1) this.bookings[idx] = updated;
        this.snack.open('Booking cancelled.', 'OK', { duration: 3000 });
        this.cancelling = null;
      },
      error: (err) => {
        this.snack.open(err?.error?.error ?? 'Cancel failed.', 'OK', { duration: 3000 });
        this.cancelling = null;
      }
    });
  }

  /** Human-readable status label */
  statusLabel(s: string): string {
    return s.replace('_', ' ');
  }

  canCancel(b: Booking): boolean {
    return b.status === 'PENDING' || b.status === 'APPROVED';
  }
}
