import { Routes } from '@angular/router';
import { authGuard }  from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  // ── Public landing page ───────────────────────────────────────────
  {
    path: '',
    title: "We'll Share — Share the Journey, Share the Cost",
    loadComponent: () =>
      import('./features/landing/landing-page/landing-page.component')
        .then(m => m.LandingPageComponent)
  },

  // ── Public auth ───────────────────────────────────────────────────
  {
    path: 'auth',
    title: "We'll Share — Sign In",
    loadChildren: () =>
      import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },

  // ── Protected — regular users ─────────────────────────────────────
  {
    path: 'dashboard',
    title: "We'll Share — Dashboard",
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES)
  },
  {
    path: 'profile',
    title: "We'll Share — My Profile",
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/profile/profile.routes').then(m => m.PROFILE_ROUTES)
  },
  {
    path: 'rides',
    title: "We'll Share — Rides",
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/rides/rides.routes').then(m => m.RIDES_ROUTES)
  },
  {
    path: 'bookings',
    title: "We'll Share — Bookings",
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/bookings/bookings.routes').then(m => m.BOOKINGS_ROUTES)
  },
  {
    path: 'parcels',
    title: "We'll Share — Parcels",
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/parcels/parcels.routes').then(m => m.PARCELS_ROUTES)
  },
  {
    path: 'history',
    title: "We'll Share — History",
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/history/history.routes').then(m => m.HISTORY_ROUTES)
  },

  // ── Admin panel ───────────────────────────────────────────────────
  {
    path: 'admin',
    title: "We'll Share — Admin",
    canActivate: [adminGuard],
    loadChildren: () =>
      import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },

  // ── Public trip-share tracking (no auth required) ─────────────────
  {
    path: 'track/:token',
    title: "We'll Share — Live Tracking",
    loadComponent: () =>
      import('./features/public/trip-track/trip-track.component')
        .then(m => m.TripTrackComponent)
  },

  // ── 404 ───────────────────────────────────────────────────────────
  {
    path: '**',
    title: "We'll Share — Page Not Found",
    loadComponent: () =>
      import('./features/not-found/not-found.component')
        .then(m => m.NotFoundComponent)
  }
];
