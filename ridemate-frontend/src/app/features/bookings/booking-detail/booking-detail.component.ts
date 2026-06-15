import { Component, OnInit } from '@angular/core';
import { CommonModule }     from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { MatCardModule }    from '@angular/material/card';
import { MatButtonModule }  from '@angular/material/button';
import { MatIconModule }    from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';

import { ApiService }  from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { Booking }     from '../../../core/models/booking.model';
import {
  ReportDialogComponent
} from '../../../shared/components/report-dialog/report-dialog.component';
import {
  ReviewDialogComponent
} from '../../../shared/components/review-dialog/review-dialog.component';
import { ChatBoxComponent } from '../chat-box/chat-box.component';

@Component({
  selector: 'app-booking-detail',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, MatSnackBarModule,
    MatDialogModule,
    ChatBoxComponent
  ],
  templateUrl: './booking-detail.component.html',
  styleUrls:   ['./booking-detail.component.scss']
})
export class BookingDetailComponent implements OnInit {

  booking:  Booking | null = null;
  loading   = true;

  bookingId!: number;
  myId!:      number;

  constructor(
    private route:  ActivatedRoute,
    private api:    ApiService,
    private auth:   AuthService,
    private snack:  MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.bookingId = Number(this.route.snapshot.paramMap.get('id'));
    this.myId      = this.auth.getCurrentSession()?.userId ?? 0;

    this.api.getBooking(this.bookingId).subscribe({
      next:  b  => { this.booking = b; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  // ── Helpers ──────────────────────────────────────────────────────

  isDriver(): boolean {
    return this.booking?.ride?.driver?.id === this.myId;
  }

  statusLabel(s: string): string { return s.replace('_', ' '); }

  // ── Report counter-party ─────────────────────────────────────────

  openReportDialog(): void {
    const b = this.booking;
    if (!b) return;

    // Determine who to report: if I am the driver, report the passenger; otherwise report the driver
    const reportedId   = this.isDriver() ? b.passenger.id   : b.ride.driver.id;
    const reportedName = this.isDriver() ? b.passenger.name : b.ride.driver.name;

    this.dialog.open(ReportDialogComponent, {
      width: '440px',
      data: { reportedUserId: reportedId, reportedUserName: reportedName, bookingId: b.id }
    });
  }

  openReviewDialog(): void {
    const b = this.booking;
    if (!b || b.status !== 'COMPLETED') return;

    // Review the counter-party
    const reviewedId   = this.isDriver() ? b.passenger.id   : b.ride.driver.id;
    const reviewedName = this.isDriver() ? b.passenger.name : b.ride.driver.name;

    this.dialog.open(ReviewDialogComponent, {
      width: '440px',
      data: { reviewedUserId: reviewedId, reviewedUserName: reviewedName, bookingId: b.id }
    });
  }
}
