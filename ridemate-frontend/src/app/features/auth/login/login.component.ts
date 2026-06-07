import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatProgressSpinnerModule, MatIconModule, MatSnackBarModule
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent {

  form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  loading = false;
  hidePassword = true;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    if (this.route.snapshot.queryParamMap.get('expired')) {
      this.snackBar.open('Your session has expired. Please log in again.', 'OK', { duration: 4000 });
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.errorMessage = '';

    const { email, password } = this.form.value;

    this.auth.login({ email: email!, password: password! }).subscribe({
      next: (res) => {
        this.loading = false;
        if (!res.phoneVerified) {
          this.router.navigate(['/auth/verify-phone']);
          return;
        }
        if (res.role === 'ADMIN') {
          this.router.navigate(['/admin/dashboard']);
          return;
        }
        // If user hasn't chosen a mode yet, send them to role-select
        const mode = this.auth.getUserMode();
        this.router.navigate([mode ? '/dashboard' : '/auth/role-select']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Login failed. Please try again.';
      }
    });
  }
}
