import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule }             from '@angular/common';
import {
  FormBuilder, FormGroup, ReactiveFormsModule, Validators
} from '@angular/forms';
import { MatDialogModule, MatDialogRef,
         MAT_DIALOG_DATA }          from '@angular/material/dialog';
import { MatFormFieldModule }       from '@angular/material/form-field';
import { MatInputModule }           from '@angular/material/input';
import { MatButtonModule }          from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService }               from '../../../core/services/api.service';
import { ReviewRequest }            from '../../../core/models/review.model';
import { StarRatingComponent }      from '../star-rating/star-rating.component';

/** Data injected when opening the dialog. */
export interface ReviewDialogData {
  reviewedUserId:   number;
  reviewedUserName: string;
  bookingId?:  number;
  parcelId?:   number;
}

/**
 * Material dialog for leaving a 1–5 star review.
 *
 * Usage:
 * ```ts
 * this.dialog.open(ReviewDialogComponent, {
 *   data: { reviewedUserId: 10, reviewedUserName: 'Priya', bookingId: 5 }
 * });
 * ```
 */
@Component({
  selector: 'app-review-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    StarRatingComponent
  ],
  template: `
    <h2 mat-dialog-title>Rate {{ data.reviewedUserName }}</h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="form">

        <!-- Star picker -->
        <div class="rating-row">
          <app-star-rating
            [value]="ratingValue"
            [readonly]="false"
            (valueChange)="onStarChange($event)">
          </app-star-rating>
          <span class="rating-label">{{ ratingLabel }}</span>
        </div>
        <div *ngIf="form.get('rating')?.invalid && form.get('rating')?.touched"
             class="star-error">
          Please select a rating.
        </div>

        <!-- Comment -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Comment (optional)</mat-label>
          <textarea matInput formControlName="comment"
                    rows="4" maxlength="500"
                    placeholder="Share your experience…"></textarea>
          <mat-hint align="end">
            {{ form.get('comment')?.value?.length ?? 0 }} / 500
          </mat-hint>
        </mat-form-field>

      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="submitting">Cancel</button>
      <button mat-flat-button color="primary"
              [disabled]="form.invalid || submitting"
              (click)="submit()">
        {{ submitting ? 'Submitting…' : 'Submit Review' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .form { display: flex; flex-direction: column; gap: 16px; min-width: 340px; }

    .rating-row {
      display: flex; align-items: center; gap: 12px;
      padding: 8px 0;
    }

    .rating-label {
      font-size: 14px; color: #555; min-width: 80px;
    }

    .star-error { font-size: 12px; color: #f44336; margin-top: -12px; }

    .full-width { width: 100%; }
  `]
})
export class ReviewDialogComponent implements OnInit {

  form!:        FormGroup;
  ratingValue   = 0;
  submitting    = false;

  readonly ratingLabels = ['', 'Terrible', 'Poor', 'Average', 'Good', 'Excellent'];
  get ratingLabel(): string { return this.ratingLabels[this.ratingValue] ?? ''; }

  constructor(
    private fb:        FormBuilder,
    private api:       ApiService,
    private snackBar:  MatSnackBar,
    public  dialogRef: MatDialogRef<ReviewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ReviewDialogData
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      rating:  [null, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['']
    });
  }

  onStarChange(value: number): void {
    this.ratingValue = value;
    this.form.get('rating')?.setValue(value);
    this.form.get('rating')?.markAsTouched();
  }

  submit(): void {
    if (this.form.invalid || this.submitting) return;
    this.submitting = true;

    const req: ReviewRequest = {
      reviewedUserId: this.data.reviewedUserId,
      rating:         this.form.value.rating,
      comment:        this.form.value.comment || undefined,
      bookingId:      this.data.bookingId,
      parcelId:       this.data.parcelId
    };

    this.api.submitReview(req).subscribe({
      next: () => {
        this.submitting = false;
        this.snackBar.open('Review submitted. Thank you!', 'OK', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: err => {
        this.submitting = false;
        const msg = err?.error?.message ?? 'Failed to submit review.';
        this.snackBar.open(msg, 'Dismiss', { duration: 4000 });
      }
    });
  }
}
