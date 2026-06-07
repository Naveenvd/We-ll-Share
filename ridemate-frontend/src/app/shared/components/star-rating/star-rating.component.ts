import {
  Component, Input, Output, EventEmitter, OnChanges, SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Reusable star-rating widget.
 *
 * Interactive (input) mode — default when readonly = false:
 *   <app-star-rating [value]="myRating" (valueChange)="myRating = $event" />
 *
 * Display-only mode:
 *   <app-star-rating [value]="4.5" [readonly]="true" />
 */
@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatTooltipModule],
  template: `
    <div class="stars" [class.interactive]="!readonly">
      <span
        *ngFor="let star of stars; let i = index"
        class="star"
        [class.filled]="isFilled(i)"
        [class.hovered]="isHovered(i)"
        [matTooltip]="!readonly ? labels[i] : ''"
        (mouseenter)="!readonly && onHover(i + 1)"
        (mouseleave)="!readonly && onHover(0)"
        (click)="!readonly && select(i + 1)">
        <mat-icon>{{ getIcon(i) }}</mat-icon>
      </span>
      <span *ngIf="showValue && value" class="rating-value">
        {{ value | number:'1.1-1' }}
      </span>
    </div>
  `,
  styles: [`
    .stars {
      display: inline-flex;
      align-items: center;
      gap: 2px;
    }
    .star mat-icon {
      font-size: 22px;
      width: 22px;
      height: 22px;
      color: #bbb;
      transition: color 0.15s, transform 0.1s;
    }
    .star.filled mat-icon  { color: #ffa726; }
    .star.hovered mat-icon { color: #ffcc02; }

    .interactive .star {
      cursor: pointer;
      &:hover mat-icon { transform: scale(1.2); }
    }

    .rating-value {
      font-size: 13px;
      color: #666;
      margin-left: 4px;
    }
  `]
})
export class StarRatingComponent implements OnChanges {

  /** Current rating value (0–5) */
  @Input() value  = 0;
  /** When true the component is read-only (display mode) */
  @Input() readonly = false;
  /** Show numeric value next to the stars */
  @Input() showValue = false;
  /** Emits the new rating when the user clicks a star */
  @Output() valueChange = new EventEmitter<number>();

  stars   = [0, 1, 2, 3, 4];
  hovered = 0;

  labels = ['Terrible', 'Poor', 'Average', 'Good', 'Excellent'];

  ngOnChanges(changes: SimpleChanges): void {
    // No extra work needed — template reads `value` directly
  }

  onHover(n: number): void { this.hovered = n; }

  select(n: number): void {
    this.valueChange.emit(n);
  }

  isFilled(index: number): boolean {
    const effective = this.hovered || this.value;
    return index < effective;
  }

  isHovered(index: number): boolean {
    return this.hovered > 0 && index < this.hovered;
  }

  getIcon(index: number): string {
    const effective = this.hovered || this.value;
    // For half-star display in readonly mode
    if (this.readonly && index < Math.ceil(effective) && index >= Math.floor(effective)) {
      return 'star_half';
    }
    return index < effective ? 'star' : 'star_border';
  }
}
