import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-reject-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Reject Verification — {{ data.userName }}</h2>
    <mat-dialog-content>
      <p style="color:#666; margin-bottom:12px;">
        Please provide a clear reason. The user will see this reason in their app.
      </p>
      <form [formGroup]="form" novalidate>
        <mat-form-field appearance="outline" style="width:100%">
          <mat-label>Rejection Reason</mat-label>
          <textarea matInput formControlName="reason" rows="4"
                    placeholder="e.g. Aadhaar document is blurry. Please re-upload a clearer photo.">
          </textarea>
          <mat-error>Reason is required</mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button (click)="ref.close()">Cancel</button>
      <button mat-raised-button color="warn"
              [disabled]="form.invalid"
              (click)="submit()">
        Confirm Rejection
      </button>
    </mat-dialog-actions>
  `,
  styles: [`mat-dialog-content { min-width: 360px; }`]
})
export class RejectDialogComponent {
  form = this.fb.group({
    reason: ['', [Validators.required, Validators.minLength(10)]]
  });

  constructor(
    public ref: MatDialogRef<RejectDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { userName: string },
    private fb: FormBuilder
  ) {}

  submit(): void {
    if (this.form.invalid) return;
    this.ref.close(this.form.value.reason!);
  }
}
