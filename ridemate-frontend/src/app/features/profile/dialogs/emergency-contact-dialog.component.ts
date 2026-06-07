import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { EmergencyContact, EmergencyContactRequest } from '../../../core/models/profile.model';

@Component({
  selector: 'app-emergency-contact-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Contact' : 'Add Emergency Contact' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" novalidate class="dialog-form">

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Full Name</mat-label>
          <input matInput formControlName="name" />
          <mat-error>Required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Mobile Number</mat-label>
          <input matInput formControlName="phone" type="tel" maxlength="10" />
          <span matTextPrefix>+91&nbsp;</span>
          <mat-error>Enter a valid 10-digit number</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Relation (e.g. Mother, Friend)</mat-label>
          <input matInput formControlName="relation" />
          <mat-error>Required</mat-error>
        </mat-form-field>

      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button (click)="ref.close()">Cancel</button>
      <button mat-raised-button color="primary"
              [disabled]="form.invalid" (click)="submit()">
        {{ isEdit ? 'Save' : 'Add Contact' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-form { display: flex; flex-direction: column; gap: 4px; min-width: 320px; }
    .full-width   { width: 100%; }
    mat-dialog-content { padding-top: 8px !important; }
  `]
})
export class EmergencyContactDialogComponent {
  isEdit: boolean;

  form = this.fb.group({
    name:     ['', Validators.required],
    phone:    ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
    relation: ['', Validators.required]
  });

  constructor(
    public ref: MatDialogRef<EmergencyContactDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EmergencyContact | null,
    private fb: FormBuilder
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({ name: data.name, phone: data.phone, relation: data.relation });
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    const req: EmergencyContactRequest = {
      name:     this.form.value.name!,
      phone:    this.form.value.phone!,
      relation: this.form.value.relation!
    };
    this.ref.close(req);
  }
}
