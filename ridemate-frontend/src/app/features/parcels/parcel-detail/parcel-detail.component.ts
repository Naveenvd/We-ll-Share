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
import { Parcel }           from '../../../core/models/parcel.model';
import { ChatMessage }      from '../../../core/models/booking.model';
import {
  ReportDialogComponent
} from '../../../shared/components/report-dialog/report-dialog.component';
import {
  ReviewDialogComponent
} from '../../../shared/components/review-dialog/review-dialog.component';
import {
  ConfirmDialogComponent, ConfirmDialogData
} from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-parcel-detail',
  standalone: true,
  imports: [
    CommonModule, RouterModule, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule,
    MatProgressSpinnerModule, MatSnackBarModule, MatDialogModule
  ],
  templateUrl: './parcel-detail.component.html',
  styleUrls:   ['./parcel-detail.component.scss']
})
export class ParcelDetailComponent implements OnInit, OnDestroy, AfterViewChecked {

  @ViewChild('messagesEnd') private messagesEnd!: ElementRef;

  parcel:   Parcel | null  = null;
  messages: ChatMessage[]  = [];
  loading   = true;
  sending   = false;
  newText   = '';

  // Complaint form
  showComplaint = false;
  complaintText = '';
  submittingComplaint = false;

  parcelId!: number;
  private myId!: number;
  private wsSub!: Subscription;
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
    this.parcelId = Number(this.route.snapshot.paramMap.get('id'));
    const session = this.auth.getCurrentSession();
    this.myId     = session?.userId ?? 0;

    this.api.getParcel(this.parcelId).subscribe({
      next:  p  => { this.parcel = p; this.loading = false; },
      error: () => { this.loading = false; }
    });

    this.api.getParcelChatHistory(this.parcelId).subscribe({
      next: msgs => { this.messages = msgs; this.scrollNeeded = true; }
    });

    this.api.markParcelChatRead(this.parcelId).subscribe();

    this.wsSub = this.ws.subscribe<ChatMessage>(
      `/topic/parcel.${this.parcelId}`
    ).subscribe(msg => {
      this.messages.push(msg);
      if (msg.senderId !== this.myId) {
        this.api.markParcelChatRead(this.parcelId).subscribe();
      }
      this.scrollNeeded = true;
    });
  }

  ngAfterViewChecked(): void {
    if (this.scrollNeeded) { this.scrollToBottom(); this.scrollNeeded = false; }
  }

  ngOnDestroy(): void { this.wsSub?.unsubscribe(); }

  send(): void {
    const text = this.newText.trim();
    if (!text || this.sending) return;
    this.sending = true;
    this.newText = '';
    this.ws.publish(`/app/chat.parcel.${this.parcelId}`, { text });
    // WS publish is synchronous — reset flag after dispatch
    this.sending = false;
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); this.send(); }
  }

  isMine(msg: ChatMessage): boolean { return msg.senderId === this.myId; }

  isSender(): boolean { return this.parcel?.sender?.id === this.myId; }

  canRaiseComplaint(): boolean {
    return !!this.parcel &&
      !['PENDING', 'REJECTED', 'CANCELLED', 'COMPLAINT_RAISED'].includes(this.parcel.status);
  }

  submitComplaint(): void {
    if (!this.complaintText.trim() || this.submittingComplaint) return;
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        'Submit Complaint',
        message:      'This will flag the parcel for admin review. This action cannot be undone.',
        confirmLabel: 'Submit',
        confirmColor: 'warn'
      }
    });
    ref.afterClosed().subscribe(confirmed => { if (confirmed) this._doSubmitComplaint(); });
  }

  private _doSubmitComplaint(): void {
    this.submittingComplaint = true;
    this.api.raiseParcelComplaint(this.parcelId, this.complaintText.trim()).subscribe({
      next: () => {
        this.snack.open('Complaint submitted. Our team will review it.', 'OK', { duration: 5000 });
        this.showComplaint  = false;
        this.complaintText  = '';
        this.submittingComplaint = false;
        // Refresh parcel to show COMPLAINT_RAISED status
        this.api.getParcel(this.parcelId).subscribe(p => this.parcel = p);
      },
      error: err => {
        this.snack.open(err?.error?.error ?? 'Failed to submit complaint.', 'OK', { duration: 4000 });
        this.submittingComplaint = false;
      }
    });
  }

  statusLabel(s: string): string { return s.replace('_', ' '); }

  // ── Report counter-party ──────────────────────────────────────────

  openReportDialog(): void {
    const p = this.parcel;
    if (!p) return;

    // As sender — report the driver; as driver — report the sender
    const reportedId   = this.isSender() ? p.ride.driver.id   : p.sender.id;
    const reportedName = this.isSender() ? p.ride.driver.name : p.sender.name;

    this.dialog.open(ReportDialogComponent, {
      width: '440px',
      data: { reportedUserId: reportedId, reportedUserName: reportedName, parcelId: p.id }
    });
  }

  openReviewDialog(): void {
    const p = this.parcel;
    if (!p || p.status !== 'DELIVERED') return;

    const reviewedId   = this.isSender() ? p.ride.driver.id   : p.sender.id;
    const reviewedName = this.isSender() ? p.ride.driver.name : p.sender.name;

    this.dialog.open(ReviewDialogComponent, {
      width: '440px',
      data: { reviewedUserId: reviewedId, reviewedUserName: reviewedName, parcelId: p.id }
    });
  }

  private scrollToBottom(): void {
    try { this.messagesEnd.nativeElement.scrollIntoView({ behavior: 'smooth' }); } catch { }
  }
}
