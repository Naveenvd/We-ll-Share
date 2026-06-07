import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatStepperModule } from '@angular/material/stepper';

import { AuthService } from '../../../core/services/auth.service';

/** Custom validator: password + confirmPassword must match */
function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const pw  = control.get('password')?.value;
  const cpw = control.get('confirmPassword')?.value;
  return pw === cpw ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatSelectModule, MatDatepickerModule, MatNativeDateModule,
    MatProgressSpinnerModule, MatIconModule, MatStepperModule
  ],
  templateUrl: './signup.component.html'
})
export class SignupComponent {

  /* Step 1 — Personal info */
  step1 = this.fb.group({
    name:   ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    email:  ['', [Validators.required, Validators.email]],
    phone:  ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
    gender: ['', Validators.required],
    dob:    [null as Date | null, Validators.required]
  });

  /* Step 2 — Password */
  step2 = this.fb.group({
    password:        ['', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(/^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@#$%^&+=!]).+$/)
    ]],
    confirmPassword: ['', Validators.required]
  }, { validators: passwordMatchValidator });

  loading = false;
  hidePassword = true;
  hideConfirm  = true;
  errorMessage = '';
  maxDob       = new Date(); // dob cannot be in the future

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {}

  /** Called after both stepper steps are completed */
  submit(): void {
    if (this.step1.invalid || this.step2.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    const dob = this.step1.value.dob as Date;
    const payload = {
      name:     this.step1.value.name!,
      email:    this.step1.value.email!,
      phone:    this.step1.value.phone!,
      gender:   this.step1.value.gender! as 'MALE' | 'FEMALE' | 'OTHER',
      dob:      this.formatDate(dob),
      password: this.step2.value.password!
    };

    this.auth.signup(payload).subscribe({
      next: () => {
        this.loading = false;
        // After signup, user needs to log in first to get a token, then verify phone
        this.router.navigate(['/auth/login'], { queryParams: { registered: true } });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Signup failed. Please try again.';
      }
    });
  }

  private formatDate(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}
