import { Component, Inject }    from '@angular/core';
import { CommonModule }         from '@angular/common';
import { MatDialogModule,
         MatDialogRef,
         MAT_DIALOG_DATA }      from '@angular/material/dialog';
import { MatButtonModule }      from '@angular/material/button';
import { MatIconModule }        from '@angular/material/icon';

export interface ConfirmDialogData {
  /** Dialog heading, e.g. "Delete vehicle" */
  title:   string;
  /** Body text, e.g. "Are you sure you want to remove this vehicle?" */
  message: string;
  /** Label for the confirm button (default: "Confirm") */
  confirmLabel?: string;
  /** Material color for the confirm button (default: "warn") */
  confirmColor?: 'primary' | 'accent' | 'warn';
}

/**
 * Generic confirmation dialog — replaces browser `confirm()` calls.
 *
 * Usage:
 * ```ts
 * const ref = this.dialog.open(ConfirmDialogComponent, {
 *   width: '380px',
 *   data: { title: 'Remove vehicle', message: 'This cannot be undone.' }
 * });
 * ref.afterClosed().subscribe(confirmed => {
 *   if (confirmed) { ... }
 * });
 * ```
 * Returns `true` if confirmed, `undefined` / `false` if dismissed.
 */
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="dialog-wrap">
      <div class="dialog-icon">
        <mat-icon color="warn">warning_amber</mat-icon>
      </div>

      <h2 mat-dialog-title>{{ data.title }}</h2>

      <mat-dialog-content>
        <p>{{ data.message }}</p>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button [mat-dialog-close]="false">Cancel</button>
        <button mat-flat-button
                [color]="data.confirmColor ?? 'warn'"
                [mat-dialog-close]="true">
          {{ data.confirmLabel ?? 'Confirm' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .dialog-wrap {
      padding: 8px 0 0;
      text-align: center;
    }
    .dialog-icon mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
    }
    h2 { margin: 12px 0 0; font-size: 18px; }
    mat-dialog-content p { color: #555; font-size: 14px; margin: 12px 0; }
    mat-dialog-actions { padding: 0 24px 20px !important; }
  `]
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {}
}
