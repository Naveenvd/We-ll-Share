import { Component, OnInit }          from '@angular/core';
import { CommonModule }                from '@angular/common';
import { RouterOutlet }                from '@angular/router';
import { AuthService }                 from './core/services/auth.service';
import { ThemeService }                from './core/services/theme.service';
import { SosButtonComponent }          from './shared/components/sos-button/sos-button.component';
import { ThemePickerComponent }        from './shared/components/theme-picker/theme-picker.component';

/**
 * Root component.
 *
 * • Renders the active route.
 * • Shows a floating SOS button on all authenticated pages (bottom-right).
 * • Shows a floating theme-picker FAB on all pages (bottom-left).
 * • Injects ThemeService early so the persisted theme is applied to <html>
 *   before any component renders.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SosButtonComponent, ThemePickerComponent],
  template: `
    <router-outlet />

    <!-- Floating SOS button — authenticated pages only (bottom-right) -->
    <app-sos-button *ngIf="isLoggedIn" />

    <!-- Floating theme picker — always visible (bottom-left) -->
    <app-theme-picker />
  `
})
export class AppComponent implements OnInit {
  isLoggedIn = false;

  constructor(
    private auth:  AuthService,
    // Inject ThemeService here so it is created (and theme applied) at
    // app bootstrap before any child component is instantiated.
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    private _theme: ThemeService,
  ) {}

  ngOnInit(): void {
    this.auth.session$.subscribe(s => {
      this.isLoggedIn = !!s;
    });
  }
}
