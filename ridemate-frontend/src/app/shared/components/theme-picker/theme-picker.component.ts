import {
  Component,
  ElementRef,
  HostListener,
} from '@angular/core';
import { CommonModule }                       from '@angular/common';
import { MatIconModule }                      from '@angular/material/icon';
import { MatTooltipModule }                   from '@angular/material/tooltip';
import { ThemeService, ColorTheme }           from '../../../core/services/theme.service';

interface ColorOption {
  key:   ColorTheme;
  label: string;
  hex:   string;
}

/**
 * Floating theme-picker FAB.
 *
 * Sits at bottom-left (SOS button lives bottom-right).
 * Click the palette icon to open a compact panel:
 *   • Light / Dark toggle
 *   • Five colour swatches
 */
@Component({
  selector: 'app-theme-picker',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatTooltipModule],
  templateUrl: './theme-picker.component.html',
  styleUrls:   ['./theme-picker.component.scss'],
})
export class ThemePickerComponent {
  isOpen = false;

  readonly colors: ColorOption[] = [
    { key: 'violet',  label: 'Violet',  hex: '#7c3aed' },
    { key: 'blue',    label: 'Blue',    hex: '#2563eb' },
    { key: 'teal',    label: 'Teal',    hex: '#0d9488' },
    { key: 'rose',    label: 'Rose',    hex: '#e11d48' },
    { key: 'emerald', label: 'Emerald', hex: '#059669' },
  ];

  constructor(
    readonly theme: ThemeService,
    private el:     ElementRef,
  ) {}

  get isDark():       boolean    { return this.theme.mode()  === 'dark'; }
  get currentColor(): ColorTheme { return this.theme.color(); }

  toggle():                 void { this.isOpen = !this.isOpen; }
  toggleMode():             void { this.theme.toggleMode(); }
  setColor(c: ColorTheme):  void { this.theme.setColor(c); }

  /** Close panel when user clicks anywhere outside this host element. */
  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent): void {
    if (this.isOpen && !this.el.nativeElement.contains(e.target as Node)) {
      this.isOpen = false;
    }
  }

  /** Close panel on ESC key. */
  @HostListener('document:keydown.escape')
  onEsc(): void { this.isOpen = false; }
}
