import { Component, OnInit }      from '@angular/core';
import { CommonModule }            from '@angular/common';
import { RouterModule }            from '@angular/router';
import { MatTabsModule }           from '@angular/material/tabs';
import { MatCardModule }           from '@angular/material/card';
import { MatIconModule }           from '@angular/material/icon';
import { MatChipsModule }          from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule }        from '@angular/material/divider';
import { MatTooltipModule }        from '@angular/material/tooltip';
import { ApiService }              from '../../../core/services/api.service';
import { HistoryItem }             from '../../../core/models/safety.model';

@Component({
  selector: 'app-history-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTabsModule,
    MatCardModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatTooltipModule
  ],
  template: `
    <div class="page-container">
      <h2 class="page-title">
        <mat-icon>history</mat-icon>
        Activity History
      </h2>

      <!-- Loading state -->
      <div *ngIf="loading" class="center-spinner">
        <mat-spinner diameter="48"></mat-spinner>
      </div>

      <ng-container *ngIf="!loading">

        <mat-tab-group animationDuration="200ms" class="tab-group">

          <!-- ── All tab ─────────────────────────────────── -->
          <mat-tab label="All ({{ all.length }})">
            <ng-container *ngTemplateOutlet="itemList; context: { items: all }">
            </ng-container>
          </mat-tab>

          <!-- ── Rides as driver ────────────────────────── -->
          <mat-tab label="As Driver ({{ asDriver.length }})">
            <ng-container *ngTemplateOutlet="itemList; context: { items: asDriver }">
            </ng-container>
          </mat-tab>

          <!-- ── Rides as passenger ─────────────────────── -->
          <mat-tab label="As Passenger ({{ asPassenger.length }})">
            <ng-container *ngTemplateOutlet="itemList; context: { items: asPassenger }">
            </ng-container>
          </mat-tab>

          <!-- ── Parcels as sender ──────────────────────── -->
          <mat-tab label="Parcels Sent ({{ asSender.length }})">
            <ng-container *ngTemplateOutlet="itemList; context: { items: asSender }">
            </ng-container>
          </mat-tab>

          <!-- ── Parcels as driver ──────────────────────── -->
          <mat-tab label="Parcels Carried ({{ asParcelDriver.length }})">
            <ng-container *ngTemplateOutlet="itemList; context: { items: asParcelDriver }">
            </ng-container>
          </mat-tab>

        </mat-tab-group>

      </ng-container>
    </div>

    <!-- ── Reusable item list template ─────────────────────────────── -->
    <ng-template #itemList let-items="items">
      <div class="item-list">

        <div *ngIf="items.length === 0" class="empty-state">
          <mat-icon>inbox</mat-icon>
          <p>Nothing here yet.</p>
        </div>

        <mat-card *ngFor="let item of items" class="history-card">
          <mat-card-content>

            <div class="card-header">
              <!-- Type chip -->
              <mat-chip [class]="chipClass(item.type)" disableRipple>
                <mat-icon>{{ typeIcon(item.type) }}</mat-icon>
                {{ typeLabel(item.type) }}
              </mat-chip>

              <!-- Status chip -->
              <mat-chip [class]="statusClass(item.status)" disableRipple>
                {{ item.status }}
              </mat-chip>
            </div>

            <!-- Route -->
            <div class="route">
              <span class="location">
                <mat-icon class="loc-icon">location_on</mat-icon>
                {{ item.fromLocation }}
              </span>
              <mat-icon class="arrow">arrow_forward</mat-icon>
              <span class="location">
                <mat-icon class="loc-icon">flag</mat-icon>
                {{ item.toLocation }}
              </span>
            </div>

            <!-- Meta row -->
            <div class="meta-row">
              <span *ngIf="item.eventTime" class="meta-item">
                <mat-icon>schedule</mat-icon>
                {{ item.eventTime | date:'dd MMM yyyy, h:mm a' }}
              </span>

              <!-- Driver earnings -->
              <span *ngIf="item.type === 'RIDE_DRIVER' && item.earnings !== undefined"
                    class="meta-item earnings">
                <mat-icon>payments</mat-icon>
                Earned ₹{{ item.earnings | number:'1.2-2' }}
              </span>

              <!-- Passenger amount -->
              <span *ngIf="item.type === 'RIDE_PASSENGER' && item.amount !== undefined"
                    class="meta-item">
                <mat-icon>payments</mat-icon>
                Paid ₹{{ item.amount | number:'1.2-2' }}
              </span>

              <!-- Parcel price (sender view) -->
              <span *ngIf="item.type === 'PARCEL_SENDER' && item.price !== undefined"
                    class="meta-item">
                <mat-icon>local_shipping</mat-icon>
                ₹{{ item.price | number:'1.2-2' }}
              </span>

              <!-- Parcel earnings (driver view) -->
              <span *ngIf="item.type === 'PARCEL_DRIVER' && item.price !== undefined"
                    class="meta-item earnings">
                <mat-icon>local_shipping</mat-icon>
                Carried · ₹{{ item.price | number:'1.2-2' }}
              </span>

              <!-- Counter-party -->
              <span *ngIf="item.counterPartyName" class="meta-item">
                <mat-icon>person</mat-icon>
                {{ counterPartyLabel(item.type) }} {{ item.counterPartyName }}
              </span>
            </div>

          </mat-card-content>

          <!-- Deep-link to original record -->
          <mat-card-actions align="end">
            <a *ngIf="item.type === 'RIDE_DRIVER'"
               [routerLink]="['/rides', item.id]"
               mat-button color="primary">View ride</a>

            <a *ngIf="item.type === 'RIDE_PASSENGER'"
               [routerLink]="['/bookings', item.id]"
               mat-button color="primary">View booking</a>

            <a *ngIf="item.type === 'PARCEL_SENDER' || item.type === 'PARCEL_DRIVER'"
               [routerLink]="['/parcels', item.id]"
               mat-button color="primary">View parcel</a>
          </mat-card-actions>
        </mat-card>

      </div>
    </ng-template>
  `,
  styles: [`
    .page-container { max-width: 860px; margin: 24px auto; padding: 0 16px; }

    .page-title {
      display: flex; align-items: center; gap: 8px;
      font-size: 22px; font-weight: 600; margin-bottom: 20px;
      mat-icon { font-size: 26px; }
    }

    .tab-group { margin-top: 8px; }

    .item-list {
      display: flex; flex-direction: column; gap: 12px;
      padding: 16px 4px;
    }

    .empty-state {
      text-align: center; padding: 48px;
      color: #9e9e9e;
      mat-icon { font-size: 48px; height: 48px; width: 48px; }
      p { margin-top: 8px; font-size: 15px; }
    }

    .history-card { border-radius: 10px; }

    .card-header {
      display: flex; gap: 8px; flex-wrap: wrap;
      margin-bottom: 10px;
    }

    /* Type chips */
    .chip-driver         { background: #e3f2fd !important; color: #1565c0 !important; }
    .chip-passenger      { background: #e8f5e9 !important; color: #2e7d32 !important; }
    .chip-sender         { background: #fff3e0 !important; color: #e65100 !important; }
    .chip-parcel-driver  { background: #f3e5f5 !important; color: #6a1b9a !important; }

    /* Status chips */
    .chip-completed  { background: #c8e6c9 !important; color: #1b5e20 !important; }
    .chip-cancelled  { background: #ffccbc !important; color: #bf360c !important; }
    .chip-rejected   { background: #fce4ec !important; color: #880e4f !important; }
    .chip-delivered  { background: #c8e6c9 !important; color: #1b5e20 !important; }
    .chip-default    { background: #f5f5f5 !important; }

    mat-chip mat-icon { font-size: 16px; height: 16px; width: 16px; margin-right: 4px; }

    .route {
      display: flex; align-items: center; gap: 8px;
      flex-wrap: wrap;
      margin: 8px 0;
    }
    .location { display: flex; align-items: center; gap: 2px; font-size: 14px; }
    .loc-icon { font-size: 16px; height: 16px; width: 16px; color: #666; }
    .arrow    { color: #9e9e9e; font-size: 18px; }

    .meta-row {
      display: flex; flex-wrap: wrap; gap: 16px;
      margin-top: 6px; color: #555;
    }
    .meta-item {
      display: flex; align-items: center; gap: 4px;
      font-size: 13px;
      mat-icon { font-size: 15px; height: 15px; width: 15px; color: #888; }
    }
    .earnings { color: #2e7d32; font-weight: 500; }

    .center-spinner {
      display: flex; justify-content: center; padding: 48px;
    }
  `]
})
export class HistoryPageComponent implements OnInit {

  loading:         boolean     = true;
  all:             HistoryItem[] = [];
  asDriver:        HistoryItem[] = [];
  asPassenger:     HistoryItem[] = [];
  asSender:        HistoryItem[] = [];
  asParcelDriver:  HistoryItem[] = [];   // F3: parcels carried as driver

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getHistory().subscribe({
      next: items => {
        this.all            = items;
        this.asDriver       = items.filter(i => i.type === 'RIDE_DRIVER');
        this.asPassenger    = items.filter(i => i.type === 'RIDE_PASSENGER');
        this.asSender       = items.filter(i => i.type === 'PARCEL_SENDER');
        this.asParcelDriver = items.filter(i => i.type === 'PARCEL_DRIVER');  // F3
        this.loading        = false;
      },
      error: () => { this.loading = false; }
    });
  }

  // ── Template helpers ──────────────────────────────────────────────────

  typeIcon(type: string): string {
    switch (type) {
      case 'RIDE_DRIVER':    return 'drive_eta';
      case 'RIDE_PASSENGER': return 'directions_car';
      case 'PARCEL_SENDER':  return 'local_shipping';
      case 'PARCEL_DRIVER':  return 'inventory_2';
      default: return 'info';
    }
  }

  typeLabel(type: string): string {
    switch (type) {
      case 'RIDE_DRIVER':    return 'Driver';
      case 'RIDE_PASSENGER': return 'Passenger';
      case 'PARCEL_SENDER':  return 'Parcel Sent';
      case 'PARCEL_DRIVER':  return 'Parcel Carried';
      default: return type;
    }
  }

  chipClass(type: string): string {
    switch (type) {
      case 'RIDE_DRIVER':    return 'chip-driver';
      case 'RIDE_PASSENGER': return 'chip-passenger';
      case 'PARCEL_SENDER':  return 'chip-sender';
      case 'PARCEL_DRIVER':  return 'chip-parcel-driver';
      default: return 'chip-default';
    }
  }

  statusClass(status: string): string {
    switch (status.toUpperCase()) {
      case 'COMPLETED': case 'DELIVERED': return 'chip-completed';
      case 'CANCELLED':                   return 'chip-cancelled';
      case 'REJECTED':                    return 'chip-rejected';
      default:                            return 'chip-default';
    }
  }

  counterPartyLabel(type: string): string {
    switch (type) {
      case 'RIDE_DRIVER':   return 'Passengers';
      case 'PARCEL_DRIVER': return 'Sender:';
      default:              return 'Driver:';
    }
  }
}
