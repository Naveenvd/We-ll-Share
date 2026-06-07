import { Routes } from '@angular/router';

export const RIDES_ROUTES: Routes = [
  {
    path: 'post',
    loadComponent: () =>
      import('./post-ride/post-ride.component').then(m => m.PostRideComponent)
  },
  {
    path: 'search',
    loadComponent: () =>
      import('./search-rides/search-rides.component').then(m => m.SearchRidesComponent)
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./ride-detail/ride-detail.component').then(m => m.RideDetailComponent)
  },
  { path: '', redirectTo: 'search', pathMatch: 'full' }
];
