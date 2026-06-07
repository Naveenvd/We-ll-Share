import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'signup',
    loadComponent: () =>
      import('./signup/signup.component').then(m => m.SignupComponent)
  },
  {
    path: 'verify-phone',
    loadComponent: () =>
      import('./verify-phone/verify-phone.component').then(m => m.VerifyPhoneComponent)
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
  },
  {
    path: 'role-select',
    loadComponent: () =>
      import('./role-select/role-select.component').then(m => m.RoleSelectComponent)
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];
