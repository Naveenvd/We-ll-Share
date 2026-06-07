import { Routes } from '@angular/router';

export const PARCELS_ROUTES: Routes = [
  {
    path: 'post',
    loadComponent: () =>
      import('./post-parcel/post-parcel.component').then(m => m.PostParcelComponent)
  },
  {
    path: 'my',
    loadComponent: () =>
      import('./my-parcels/my-parcels.component').then(m => m.MyParcelsComponent)
  },
  {
    path: 'driver',
    loadComponent: () =>
      import('./driver-parcels/driver-parcels.component').then(m => m.DriverParcelsComponent)
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./parcel-detail/parcel-detail.component').then(m => m.ParcelDetailComponent)
  },
  { path: '', redirectTo: 'my', pathMatch: 'full' }
];
