import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Vehicle, VehicleRequest } from '../../../core/models/profile.model';

@Component({
  selector: 'app-vehicle-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Vehicle' : 'Add Vehicle' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" novalidate class="dialog-form">

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Vehicle Model</mat-label>
          <input matInput formControlName="model" placeholder="e.g. Honda City" />
          <mat-error>Required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Number Plate</mat-label>
          <input matInput formControlName="numberPlate" placeholder="MH12AB1234"
                 style="text-transform:uppercase" />
          <mat-error>Enter a valid Indian plate (e.g. MH12AB1234)</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Color</mat-label>
          <input matInput formControlName="color" placeholder="White" />
          <mat-error>Required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Total Seats (excluding driver)</mat-label>
          <input matInput formControlName="seats" type="number" min="1" max="10" />
          <mat-error>Between 1 and 10</mat-error>
        </mat-form-field>

      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button (click)="ref.close()">Cancel</button>
      <button mat-raised-button color="primary"
              [disabled]="form.invalid" (click)="submit()">
        {{ isEdit ? 'Save Changes' : 'Add Vehicle' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-form { display: flex; flex-direction: column; gap: 4px; min-width: 320px; }
    .full-width   { width: 100%; }
    mat-dialog-content { padding-top: 8px !important; }
  `]
})
export class VehicleDialogComponent {
  isEdit: boolean;

  form = this.fb.group({
    model:       ['', Validators.required],
    numberPlate: ['', [Validators.required,
                       Validators.pattern(/^[A-Z]{2}\d{2}[A-Z]{1,2}\d{4}$/i)]],
    color:       ['', Validators.required],
    seats:       [1,  [Validators.required, Validators.min(1), Validators.max(10)]]
  });

  constructor(
    public ref: MatDialogRef<VehicleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Vehicle | null,
    private fb: FormBuilder
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        model: data.model, numberPlate: data.numberPlate,
        color: data.color, seats: data.seats
      });
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    const req: VehicleRequest = {
      model:       this.form.value.model!,
      numberPlate: this.form.value.numberPlate!.toUpperCase(),
      color:       this.form.value.color!,
      seats:       Number(this.form.value.seats)
    };
    this.ref.close(req);
  }
}
