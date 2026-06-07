import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
  },
  {
    path: 'verifications',
    loadComponent: () =>
      import('./verification-queue/verification-queue.component').then(m => m.VerificationQueueComponent)
  },
  {
    path: 'users',
    loadComponent: () =>
      import('./user-management/user-management.component').then(m => m.UserManagementComponent)
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];
