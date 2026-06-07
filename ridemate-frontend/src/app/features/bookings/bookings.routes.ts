import { Routes } from '@angular/router';

export const BOOKINGS_ROUTES: Routes = [
  {
    path: 'my',
    loadComponent: () =>
      import('./my-bookings/my-bookings.component').then(m => m.MyBookingsComponent)
  },
  {
    path: 'driver',
    loadComponent: () =>
      import('./driver-bookings/driver-bookings.component').then(m => m.DriverBookingsComponent)
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./booking-detail/booking-detail.component').then(m => m.BookingDetailComponent)
  },
  { path: '', redirectTo: 'my', pathMatch: 'full' }
];
