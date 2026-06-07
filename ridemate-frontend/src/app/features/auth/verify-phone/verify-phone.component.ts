import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-verify-phone',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatProgressSpinnerModule, MatIconModule, MatSnackBarModule
  ],
  templateUrl: './verify-phone.component.html'
})
export class VerifyPhoneComponent implements OnInit, OnDestroy {

  form = this.fb.group({
    otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  loading     = false;
  resending   = false;
  errorMessage = '';

  // Countdown timer for resend button (60 seconds)
  resendCountdown = 60;
  private timer: ReturnType<typeof setInterval> | null = null;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // If phone is already verified, redirect away
    const session = this.auth.getCurrentSession();
    if (session?.phoneVerified) {
      this.router.navigate(['/dashboard']);
      return;
    }
    this.startCountdown();
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.errorMessage = '';

    this.auth.verifyPhone(this.form.value.otp!).subscribe({
      next: () => {
        this.loading = false;
        this.snackBar.open('Phone verified! Your account is pending admin approval.', 'OK', {
          duration: 5000
        });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Invalid OTP. Please try again.';
      }
    });
  }

  resend(): void {
    if (this.resendCountdown > 0) return;
    this.resending = true;

    this.auth.resendOtp().subscribe({
      next: () => {
        this.resending = false;
        this.snackBar.open('OTP resent — check the server console.', 'OK', { duration: 3000 });
        this.resendCountdown = 60;
        this.startCountdown();
      },
      error: () => {
        this.resending = false;
        this.snackBar.open('Failed to resend OTP. Try again.', 'Close', { duration: 3000 });
      }
    });
  }

  private startCountdown(): void {
    this.clearTimer();
    this.timer = setInterval(() => {
      this.resendCountdown--;
      if (this.resendCountdown <= 0) {
        this.resendCountdown = 0;
        this.clearTimer();
      }
    }, 1000);
  }

  private clearTimer(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }
}
