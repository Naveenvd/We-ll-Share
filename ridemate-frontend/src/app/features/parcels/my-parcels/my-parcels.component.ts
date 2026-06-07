import { Component, OnInit } from '@angular/core';
import { CommonModule }      from '@angular/common';
import { RouterModule }      from '@angular/router';
import { MatCardModule }     from '@angular/material/card';
import { MatButtonModule }   from '@angular/material/button';
import { MatIconModule }     from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog }    from '@angular/material/dialog';

import { ApiService }  from '../../../core/services/api.service';
import { Parcel }      from '../../../core/models/parcel.model';
import {
  ConfirmDialogComponent, ConfirmDialogData
} from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-my-parcels',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, MatSnackBarModule, MatDialogModule
  ],
  templateUrl: './my-parcels.component.html',
  styleUrls:   ['./my-parcels.component.scss']
})
export class MyParcelsComponent implements OnInit {

  parcels:   Parcel[] = [];
  loading    = true;
  cancelling: number | null = null;

  constructor(
    private api:    ApiService,
    private snack:  MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.api.getMySentParcels().subscribe({
      next:  p  => { this.parcels = p; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  cancel(id: number): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        'Cancel Parcel',
        message:      'Cancel this parcel request? This action cannot be undone.',
        confirmLabel: 'Cancel Parcel',
        confirmColor: 'warn'
      }
    });
    ref.afterClosed().subscribe(confirmed => { if (confirmed) this._doCancel(id); });
  }

  private _doCancel(id: number): void {
    this.cancelling = id;
    this.api.cancelParcel(id).subscribe({
      next: updated => {
        const i = this.parcels.findIndex(p => p.id === id);
        if (i !== -1) this.parcels[i] = updated;
        this.snack.open('Parcel cancelled.', 'OK', { duration: 3000 });
        this.cancelling = null;
      },
      error: err => {
        this.snack.open(err?.error?.error ?? 'Error', 'OK', { duration: 3000 });
        this.cancelling = null;
      }
    });
  }

  canCancel(p: Parcel): boolean {
    return p.status === 'PENDING' || p.status === 'ACCEPTED';
  }

  statusLabel(s: string): string { return s.replace('_', ' '); }
}
