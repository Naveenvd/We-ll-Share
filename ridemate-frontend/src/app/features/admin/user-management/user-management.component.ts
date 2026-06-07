import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';

import { ApiService } from '../../../core/services/api.service';
import { AdminUserSummary } from '../../../core/models/profile.model';
import {
  ConfirmDialogComponent, ConfirmDialogData
} from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive,
    MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatSnackBarModule, MatPaginatorModule, MatFormFieldModule, MatInputModule,
    MatChipsModule, MatTableModule, MatTooltipModule, MatDialogModule
  ],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {

  users: AdminUserSummary[] = [];
  totalElements = 0;
  pageSize = 20;
  loading = false;
  searchCtrl = new FormControl('');
  displayedColumns = ['name','email','phone','status','joined','actions'];
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
    // Live search with 400ms debounce
    this.searchCtrl.valueChanges.pipe(
      debounceTime(400), distinctUntilChanged()
    ).subscribe(() => this.load(0));
  }

  load(page: number): void {
    this.loading = true;
    this.api.listAdminUsers(this.searchCtrl.value ?? '', page, this.pageSize).subscribe({
      next: (res) => {
        this.users = res.content;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  onPage(e: PageEvent): void { this.load(e.pageIndex); }

  suspend(u: AdminUserSummary): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        `Suspend ${u.name}`,
        message:      `Suspend ${u.name}? They will be unable to log in or use the platform.`,
        confirmLabel: 'Suspend',
        confirmColor: 'warn'
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.api.suspendUser(u.id).subscribe({
        next: (updated) => {
          this.users = this.users.map(x => x.id === updated.id ? updated : x);
          this.snack.open(`${updated.name} suspended.`, 'OK', { duration: 3000 });
        },
        error: err => this.snack.open(err?.error?.error ?? 'Failed.', 'Close', { duration: 3000 })
      });
    });
  }

  unblock(u: AdminUserSummary): void {
    this.api.unblockUser(u.id).subscribe({
      next: (updated) => {
        this.users = this.users.map(x => x.id === updated.id ? updated : x);
        this.snack.open(`${updated.name} unblocked.`, 'OK', { duration: 3000 });
      }
    });
  }
}
