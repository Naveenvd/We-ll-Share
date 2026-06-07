import { Component, OnInit }    from '@angular/core';
import { CommonModule }          from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule }           from '@angular/forms';
import { MatCardModule }         from '@angular/material/card';
import { MatButtonModule }       from '@angular/material/button';
import { MatIconModule }         from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule }         from '@angular/material/tabs';
import { MatChipsModule }        from '@angular/material/chips';
import { MatInputModule }        from '@angular/material/input';
import { MatFormFieldModule }    from '@angular/material/form-field';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService }           from '../../../core/services/auth.service';
import { ApiService }            from '../../../core/services/api.service';
import { AdminDashboardStats }   from '../../../core/models/profile.model';
import { SosAlertResponse, ReportResponse } from '../../../core/models/safety.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule, RouterLink, RouterLinkActive, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatTabsModule, MatChipsModule, MatInputModule, MatFormFieldModule,
    MatSnackBarModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {

  stats:           AdminDashboardStats | null = null;
  sosAlerts:       SosAlertResponse[]  = [];
  reports:         ReportResponse[]    = [];
  loading          = true;
  isMobileMenuOpen = false;

  /** Per-report resolution text keyed by report id */
  resolutionText: Record<number, string | undefined> = {};

  constructor(
    private api:   ApiService,
    private auth:  AuthService,
    private router: Router,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.api.getAdminStats().subscribe({
      next: (s) => { this.stats = s; this.loading = false; },
      error: ()  => { this.loading = false; }
    });

    this.api.getAdminOpenSos().subscribe(list => this.sosAlerts = list);
    this.api.getAdminOpenReports().subscribe(list => this.reports = list);
  }

  toggleMobileMenu(): void { this.isMobileMenuOpen = !this.isMobileMenuOpen; }
  closeMobileMenu():  void { this.isMobileMenuOpen = false; }

  // ── SOS ──────────────────────────────────────────────────────────

  acknowledge(alert: SosAlertResponse): void {
    this.api.acknowledgeAdminSos(alert.id).subscribe({
      next: () => {
        this.sosAlerts = this.sosAlerts.filter(a => a.id !== alert.id);
        this.snack.open('SOS alert acknowledged.', 'OK', { duration: 3000 });
        if (this.stats) this.stats.openSosAlerts = Math.max(0, this.stats.openSosAlerts - 1);
      },
      error: err => this.snack.open(err?.error?.error ?? 'Failed.', 'OK', { duration: 3000 })
    });
  }

  // ── Reports ───────────────────────────────────────────────────────

  resolveReport(report: ReportResponse): void {
    const resolution = (this.resolutionText[report.id] ?? '').trim();
    if (!resolution) {
      this.snack.open('Enter a resolution note first.', 'OK', { duration: 3000 });
      return;
    }
    this.api.resolveAdminReport(report.id, resolution).subscribe({
      next: () => {
        this.reports = this.reports.filter(r => r.id !== report.id);
        delete this.resolutionText[report.id];
        this.snack.open('Report resolved.', 'OK', { duration: 3000 });
        if (this.stats) this.stats.openReports = Math.max(0, this.stats.openReports - 1);
      },
      error: err => this.snack.open(err?.error?.error ?? 'Failed.', 'OK', { duration: 3000 })
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/auth/login']);
  }
}
