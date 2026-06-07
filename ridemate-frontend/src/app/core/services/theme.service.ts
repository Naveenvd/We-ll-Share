import { Injectable, signal } from '@angular/core';

export type AppTheme   = 'light' | 'dark';
export type ColorTheme = 'violet' | 'blue' | 'teal' | 'rose' | 'emerald';

interface PersistedTheme {
  mode:  AppTheme;
  color: ColorTheme;
}

/**
 * ThemeService — manages light/dark mode and accent-color theme.
 *
 * Persists to localStorage; applies `data-theme` and `data-color`
 * attributes on <html> so CSS custom-property selectors react, and
 * toggles `dark-mode` class on <body> for Angular Material's dark-theme include.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly SK = 'rm_theme';

  /** Reactive signals — read these in templates with `theme.mode()` */
  readonly mode  = signal<AppTheme>('light');
  readonly color = signal<ColorTheme>('violet');

  constructor() {
    this.load();
  }

  // ── Public API ──────────────────────────────────────────────────

  toggleMode(): void              { this.setMode(this.mode() === 'light' ? 'dark' : 'light'); }
  setMode(m: AppTheme): void      { this.mode.set(m);  this.apply(); this.save(); }
  setColor(c: ColorTheme): void   { this.color.set(c); this.apply(); this.save(); }

  // ── Private helpers ─────────────────────────────────────────────

  private apply(): void {
    const root = document.documentElement;
    root.setAttribute('data-theme', this.mode());
    root.setAttribute('data-color', this.color());
    document.body.classList.toggle('dark-mode', this.mode() === 'dark');
  }

  private save(): void {
    const val: PersistedTheme = { mode: this.mode(), color: this.color() };
    localStorage.setItem(this.SK, JSON.stringify(val));
  }

  private load(): void {
    try {
      const raw = localStorage.getItem(this.SK);
      if (raw) {
        const { mode, color } = JSON.parse(raw) as PersistedTheme;
        this.mode.set(mode   ?? 'light');
        this.color.set(color ?? 'violet');
      }
    } catch {
      /* ignore corrupt storage */
    }
    this.apply();   // always sync DOM immediately
  }
}
