import { Component, Input, OnDestroy } from '@angular/core';
import { CommonModule }          from '@angular/common';
import { MatButtonModule }       from '@angular/material/button';
import { MatIconModule }         from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule }      from '@angular/material/tooltip';
import { ApiService }            from '../../../core/services/api.service';
import { SosRequest }            from '../../../core/models/safety.model';

/**
 * Floating SOS button — place once inside app.component or any
 * authenticated shell layout. Grabs the device location (if available)
 * and fires a server-side alert.
 *
 * @Input bookingId — optional booking context
 * @Input parcelId  — optional parcel context
 *
 * Usage:
 *   <app-sos-button [bookingId]="booking.id" />
 */
@Component({
  selector: 'app-sos-button',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  template: `
    <button
      mat-fab
      color="warn"
      class="sos-fab"
      matTooltip="Send SOS alert"
      [disabled]="sending"
      (click)="sendSos()">
      <mat-icon>emergency</mat-icon>
    </button>
  `,
  styles: [`
    .sos-fab {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 1000;
      background: #d32f2f !important;
      box-shadow: 0 4px 12px rgba(211,47,47,.5);
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0%   { box-shadow: 0 0 0 0 rgba(211,47,47,.5); }
      70%  { box-shadow: 0 0 0 10px rgba(211,47,47,0); }
      100% { box-shadow: 0 0 0 0 rgba(211,47,47,0); }
    }
    .sos-fab[disabled] { opacity: .6; animation: none; }
  `]
})
export class SosButtonComponent implements OnDestroy {

  @Input() bookingId?: number;
  @Input() parcelId?:  number;

  sending = false;

  constructor(
    private api:      ApiService,
    private snackBar: MatSnackBar
  ) {}

  sendSos(): void {
    if (this.sending) return;
    this.sending = true;

    // Try to get geolocation; fall back to null coordinates if denied
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        pos => this.dispatchAlert(pos.coords.latitude, pos.coords.longitude),
        ()  => this.dispatchAlert(null, null),
        { timeout: 5000 }
      );
    } else {
      this.dispatchAlert(null, null);
    }
  }

  private dispatchAlert(lat: number | null, lng: number | null): void {
    const req: SosRequest = {
      latitude:  lat,
      longitude: lng,
      bookingId: this.bookingId,
      parcelId:  this.parcelId
    };

    this.api.triggerSos(req).subscribe({
      next: () => {
        this.sending = false;
        this.snackBar.open(
          '🚨 SOS alert sent! Emergency contacts have been notified.',
          'OK',
          { duration: 6000, panelClass: 'snack-error' }
        );
      },
      error: () => {
        this.sending = false;
        this.snackBar.open(
          'Failed to send SOS. Please call emergency services directly.',
          'Dismiss',
          { duration: 8000, panelClass: 'snack-error' }
        );
      }
    });
  }

  ngOnDestroy(): void {}
}
