import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule }             from '@angular/common';
import {
  FormBuilder, FormGroup, ReactiveFormsModule, Validators
} from '@angular/forms';
import { MatDialogModule, MatDialogRef,
         MAT_DIALOG_DATA }          from '@angular/material/dialog';
import { MatFormFieldModule }       from '@angular/material/form-field';
import { MatInputModule }           from '@angular/material/input';
import { MatSelectModule }          from '@angular/material/select';
import { MatButtonModule }          from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService }               from '../../../core/services/api.service';
import {
  REPORT_REASONS, ReportRequest
} from '../../../core/models/safety.model';

/** Data passed when opening this dialog. */
export interface ReportDialogData {
  /** The user being reported */
  reportedUserId:   number;
  reportedUserName: string;
  /** Optional context linkage */
  bookingId?: number;
  parcelId?:  number;
}

/**
 * Reusable Material dialog for reporting a user.
 *
 * Open from any component:
 * ```ts
 * this.dialog.open(ReportDialogComponent, {
 *   data: { reportedUserId: 42, reportedUserName: 'Raj', bookingId: 7 }
 * });
 * ```
 */
@Component({
  selector: 'app-report-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule
  ],
  template: `
    <h2 mat-dialog-title>Report {{ data.reportedUserName }}</h2>

    <mat-dialog-content>
      <p class="hint">
        Reports are reviewed by our safety team.
        Submitting a false report may result in account action.
      </p>

      <form [formGroup]="form" class="form">

        <!-- Reason category -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Reason</mat-label>
          <mat-select formControlName="reason" required>
            <mat-option *ngFor="let r of reasons" [value]="r.value">
              {{ r.label }}
            </mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('reason')?.hasError('required')">
            Please select a reason.
          </mat-error>
        </mat-form-field>

        <!-- Free-text details -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Additional details (optional)</mat-label>
          <textarea matInput formControlName="details"
                    rows="4" maxlength="500"
                    placeholder="Describe what happened..."></textarea>
          <mat-hint align="end">
            {{ form.get('details')?.value?.length ?? 0 }} / 500
          </mat-hint>
        </mat-form-field>

      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="submitting">
        Cancel
      </button>
      <button mat-flat-button color="warn"
              [disabled]="form.invalid || submitting"
              (click)="submit()">
        {{ submitting ? 'Submitting…' : 'Submit report' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .hint   { font-size: 13px; color: #666; margin-bottom: 16px; }
    .form   { display: flex; flex-direction: column; gap: 8px; min-width: 340px; }
    .full-width { width: 100%; }
  `]
})
export class ReportDialogComponent implements OnInit {

  form!:       FormGroup;
  reasons =    REPORT_REASONS;
  submitting = false;

  constructor(
    private fb:        FormBuilder,
    private api:       ApiService,
    private snackBar:  MatSnackBar,
    public  dialogRef: MatDialogRef<ReportDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ReportDialogData
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      reason:  ['', Validators.required],
      details: ['']
    });
  }

  submit(): void {
    if (this.form.invalid || this.submitting) return;
    this.submitting = true;

    const req: ReportRequest = {
      reportedUserId: this.data.reportedUserId,
      reason:         this.form.value.reason,
      details:        this.form.value.details || undefined,
      bookingId:      this.data.bookingId,
      parcelId:       this.data.parcelId
    };

    this.api.reportUser(req).subscribe({
      next: () => {
        this.submitting = false;
        this.snackBar.open('Report submitted. Our team will review it.', 'OK',
          { duration: 4000 });
        this.dialogRef.close(true);
      },
      error: err => {
        this.submitting = false;
        const msg = err?.error?.message ?? 'Failed to submit report.';
        this.snackBar.open(msg, 'Dismiss', { duration: 4000 });
      }
    });
  }
}
