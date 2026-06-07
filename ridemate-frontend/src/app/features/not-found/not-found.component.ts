import { Component }   from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule }   from '@angular/material/icon';

/**
 * 404 Not Found page — shown when the router matches "**".
 * Provides back-navigation to dashboard or login.
 */
@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule],
  template: `
    <div class="nf-wrapper">
      <div class="nf-card">
        <mat-icon class="nf-icon">directions_car_off</mat-icon>
        <h1>404</h1>
        <h2>Page not found</h2>
        <p>
          Looks like this road doesn't exist.
          Let's get you back on track.
        </p>
        <div class="nf-actions">
          <a mat-flat-button color="primary" routerLink="/dashboard">
            <mat-icon>home</mat-icon> Go to Dashboard
          </a>
          <button mat-stroked-button onclick="history.back()">
            <mat-icon>arrow_back</mat-icon> Go Back
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .nf-wrapper {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #e8eaf6 0%, #f5f5f5 100%);
      padding: 24px;
    }

    .nf-card {
      text-align: center;
      max-width: 420px;
    }

    .nf-icon {
      font-size: 80px;
      width: 80px;
      height: 80px;
      color: #9fa8da;
      margin-bottom: 8px;
    }

    h1 {
      font-size: 96px;
      font-weight: 900;
      color: #1a237e;
      margin: 0;
      line-height: 1;
      letter-spacing: -4px;
    }

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: #37474f;
      margin: 12px 0 8px;
    }

    p {
      color: #78909c;
      font-size: 15px;
      line-height: 1.6;
      margin: 0 0 28px;
    }

    .nf-actions {
      display: flex;
      justify-content: center;
      gap: 12px;
      flex-wrap: wrap;
    }

    a mat-icon, button mat-icon {
      margin-right: 6px;
      font-size: 18px;
      width: 18px;
      height: 18px;
    }
  `]
})
export class NotFoundComponent {}
