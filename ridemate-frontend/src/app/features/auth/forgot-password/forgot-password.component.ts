import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';

import { AuthService } from '../../../core/services/auth.service';

function passwordMatchValidator(ctrl: AbstractControl): ValidationErrors | null {
  const pw  = ctrl.get('newPassword')?.value;
  const cpw = ctrl.get('confirmPassword')?.value;
  return pw === cpw ? null : { passwordMismatch: true };
}

/** 3-step forgot-password flow:
 *  1. Enter email  →  backend sends OTP (console mock)
 *  2. Enter OTP
 *  3. Set new password
 */
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatProgressSpinnerModule, MatIconModule, MatSnackBarModule, MatStepperModule
  ],
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent {

  // Step 1 — email
  emailForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  // Step 2 — OTP
  otpForm = this.fb.group({
    otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  // Step 3 — new password
  passwordForm = this.fb.group({
    newPassword:     ['', [
      Validators.required, Validators.minLength(8),
      Validators.pattern(/^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@#$%^&+=!]).+$/)
    ]],
    confirmPassword: ['', Validators.required]
  }, { validators: passwordMatchValidator });

  loading      = false;
  hideNew      = true;
  hideConfirm  = true;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  /** Step 1 submit — request OTP */
  sendOtp(stepper: { next: () => void }): void {
    if (this.emailForm.invalid) return;
    this.loading = true;
    this.errorMessage = '';

    this.auth.forgotPassword(this.emailForm.value.email!).subscribe({
      next: () => {
        this.loading = false;
        stepper.next();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Could not send OTP. Check the email address.';
      }
    });
  }

  /** Step 3 submit — reset password */
  resetPassword(stepper: { next: () => void }): void {
    if (this.passwordForm.invalid) return;
    this.loading = true;
    this.errorMessage = '';

    this.auth.resetPassword(
      this.emailForm.value.email!,
      this.otpForm.value.otp!,
      this.passwordForm.value.newPassword!
    ).subscribe({
      next: () => {
        this.loading = false;
        stepper.next();                 // go to success step
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Password reset failed. OTP may have expired.';
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/auth/login']);
  }
}
