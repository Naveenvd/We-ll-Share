import {
  Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked
} from '@angular/core';
import { CommonModule }     from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule }      from '@angular/forms';
import { MatCardModule }    from '@angular/material/card';
import { MatButtonModule }  from '@angular/material/button';
import { MatIconModule }    from '@angular/material/icon';
import { MatInputModule }   from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { Subscription }     from 'rxjs';

import { ApiService }       from '../../../core/services/api.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { AuthService }      from '../../../core/services/auth.service';
import { Booking, ChatMessage } from '../../../core/models/booking.model';
import {
  ReportDialogComponent
} from '../../../shared/components/report-dialog/report-dialog.component';
import {
  ReviewDialogComponent
} from '../../../shared/components/review-dialog/review-dialog.component';

@Component({
  selector: 'app-booking-detail',
  standalone: true,
  imports: [
    CommonModule, RouterModule, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule,
    MatProgressSpinnerModule, MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './booking-detail.component.html',
  styleUrls:   ['./booking-detail.component.scss']
})
export class BookingDetailComponent implements OnInit, OnDestroy, AfterViewChecked {

  @ViewChild('messagesEnd') private messagesEnd!: ElementRef;

  booking:  Booking | null = null;
  messages: ChatMessage[]  = [];
  loading   = true;
  sending   = false;
  newText   = '';

  bookingId!: number;
  private myEmail!:   string;
  private myId!:      number;
  private wsSub!:     Subscription;
  private scrollNeeded = false;

  constructor(
    private route:  ActivatedRoute,
    private api:    ApiService,
    private ws:     WebSocketService,
    private auth:   AuthService,
    private snack:  MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.bookingId = Number(this.route.snapshot.paramMap.get('id'));
    const session  = this.auth.getCurrentSession();
    this.myEmail   = session?.email  ?? '';
    this.myId      = session?.userId ?? 0;

    // Load booking + history in parallel
    this.api.getBooking(this.bookingId).subscribe({
      next:  b  => { this.booking = b; this.loading = false; },
      error: () => { this.loading = false; }
    });

    this.api.getChatHistory(this.bookingId).subscribe({
      next: msgs => {
        this.messages = msgs;
        this.scrollNeeded = true;
      }
    });

    // Mark messages as read
    this.api.markChatRead(this.bookingId).subscribe();

    // Subscribe to real-time updates
    this.wsSub = this.ws.subscribe<ChatMessage>(
      `/topic/booking.${this.bookingId}`
    ).subscribe(msg => {
      this.messages.push(msg);
      // If the incoming message is from the other party, mark it read
      if (msg.senderId !== this.myId) {
        this.api.markChatRead(this.bookingId).subscribe();
      }
      this.scrollNeeded = true;
    });
  }

  ngAfterViewChecked(): void {
    if (this.scrollNeeded) {
      this.scrollToBottom();
      this.scrollNeeded = false;
    }
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
  }

  // ── Send a message ───────────────────────────────────────────────

  send(): void {
    const text = this.newText.trim();
    if (!text || this.sending) return;
    this.sending = true;
    this.newText = '';

    this.ws.publish(`/app/chat.booking.${this.bookingId}`, { text });
    // The WS subscription will receive the echo and add it to the list
    this.sending = false;
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────

  isMine(msg: ChatMessage): boolean {
    return msg.senderId === this.myId;
  }

  isDriver(): boolean {
    return this.booking?.ride?.driver?.id === this.myId;
  }

  private scrollToBottom(): void {
    try {
      this.messagesEnd.nativeElement.scrollIntoView({ behavior: 'smooth' });
    } catch { /* ignore */ }
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
