import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { ApiService } from '../../../core/services/api.service';
import { AdminUserSummary } from '../../../core/models/profile.model';
import { RejectDialogComponent } from '../dialogs/reject-dialog.component';

@Component({
  selector: 'app-verification-queue',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive,
    MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatSnackBarModule, MatChipsModule, MatDialogModule, MatPaginatorModule,
    MatDividerModule, MatFormFieldModule, MatInputModule
  ],
  templateUrl: './verification-queue.component.html',
  styleUrls: ['./verification-queue.component.scss']
})
export class VerificationQueueComponent implements OnInit {

  users: AdminUserSummary[] = [];
  totalElements = 0;
  pageSize = 10;
  loading = false;
  isMobileMenuOpen = false;

  constructor(
    private api:    ApiService,
    private snack:  MatSnackBar,
    private dialog: MatDialog,
    private auth:   AuthService,
    private router: Router
  ) {}

  toggleMobileMenu(): void { this.isMobileMenuOpen = !this.isMobileMenuOpen; }
  closeMobileMenu():  void { this.isMobileMenuOpen = false; }
  logout(): void { this.auth.logout(); this.router.navigate(['/auth/login']); }

  ngOnInit(): void {
    this.load(0);
  }

  load(page: number): void {
    this.loading = true;
    this.api.getPendingVerifications(page, this.pageSize).subscribe({
      next: (res) => {
        this.users = res.content;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  onPage(e: PageEvent): void {
    this.load(e.pageIndex);
  }

  approve(user: AdminUserSummary): void {
    this.api.verifyUser(user.id, true).subscribe({
      next: (updated) => {
        this.users = this.users.filter(u => u.id !== updated.id);
        this.totalElements--;
        this.snack.open(`${updated.name} approved.`, 'OK', { duration: 3000 });
      },
      error: (err) => this.snack.open(err.error?.error || 'Failed.', 'Close', { duration: 3000 })
    });
  }

  reject(user: AdminUserSummary): void {
    const ref = this.dialog.open(RejectDialogComponent, {
      width: '420px',
      data: { userName: user.name }
    });
    ref.afterClosed().subscribe((reason: string | undefined) => {
      if (!reason) return;
      this.api.verifyUser(user.id, false, reason).subscribe({
        next: (updated) => {
          this.users = this.users.filter(u => u.id !== updated.id);
          this.totalElements--;
          this.snack.open(`${updated.name} rejected.`, 'OK', { duration: 3000 });
        },
        error: (err) => this.snack.open(err.error?.error || 'Failed.', 'Close', { duration: 3000 })
      });
    });
  }
}
