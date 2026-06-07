import { Component, OnInit } from '@angular/core';
import { CommonModule }       from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { forkJoin }           from 'rxjs';
import { MatCardModule }      from '@angular/material/card';
import { MatButtonModule }    from '@angular/material/button';
import { MatIconModule }      from '@angular/material/icon';
import { MatChipsModule }     from '@angular/material/chips';
import { MatBadgeModule }     from '@angular/material/badge';
import { MatTooltipModule }   from '@angular/material/tooltip';
import { AuthService }        from '../../../core/services/auth.service';
import { ApiService }         from '../../../core/services/api.service';
import { SessionUser, UserMode } from '../../../core/models/auth.model';
import { Booking }            from '../../../core/models/booking.model';
import { Parcel }             from '../../../core/models/parcel.model';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [
    CommonModule, RouterLink, RouterLinkActive,
    MatCardModule, MatButtonModule, MatIconModule,
    MatChipsModule, MatBadgeModule, MatTooltipModule
  ],
  templateUrl: './dashboard-home.component.html',
  styleUrls:   ['./dashboard-home.component.scss']
})
export class DashboardHomeComponent implements OnInit {

  session: SessionUser | null = null;
  today = new Date();

  pendingBookings  = 0;
  pendingParcels   = 0;
  unreadMessages   = 0;
  isMobileMenuOpen = false;

  constructor(
    private auth:   AuthService,
    private api:    ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.session = this.auth.getCurrentSession();
    // Redirect to role-select if mode not chosen yet
    if (!this.session?.userMode) {
      this.router.navigate(['/auth/role-select']);
      return;
    }
    if (this.session?.status === 'VERIFIED') {
      this.loadBadges();
    }
  }

  get firstName(): string {
    return this.session?.name?.split(' ')[0] ?? '';
  }

  get userMode(): UserMode | undefined {
    return this.session?.userMode;
  }

  get isDriver(): boolean { return this.session?.userMode === 'DRIVER'; }
  get isRider():  boolean { return this.session?.userMode === 'RIDER';  }

  toggleMobileMenu(): void { this.isMobileMenuOpen = !this.isMobileMenuOpen; }
  closeMobileMenu():  void { this.isMobileMenuOpen = false; }

  switchMode(): void {
    const next: UserMode = this.isDriver ? 'RIDER' : 'DRIVER';
    this.auth.setUserMode(next);
    this.session = this.auth.getCurrentSession();
  }

  private loadBadges(): void {
    forkJoin({
      bookings:   this.api.getPendingDriverBookings(),
      parcels:    this.api.getPendingDriverParcels(),
      myBookings: this.api.getMyBookings(),
      myParcels:  this.api.getMySentParcels()
    }).subscribe({
      next: ({ bookings, parcels, myBookings, myParcels }) => {
        this.pendingBookings = bookings.length;
        this.pendingParcels  = parcels.length;
        const bookingUnread  = (myBookings as Booking[]).reduce((s, b) => s + (b.unreadMessages ?? 0), 0);
        const parcelUnread   = (myParcels  as Parcel[]).reduce((s, p) => s + (p.unreadMessages ?? 0), 0);
        this.unreadMessages  = bookingUnread + parcelUnread;
      }
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/auth/login']);
  }
}
