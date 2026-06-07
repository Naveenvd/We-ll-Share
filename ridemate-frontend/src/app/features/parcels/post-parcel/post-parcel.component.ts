import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }   from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

import { MatStepperModule, MatStepper } from '@angular/material/stepper';
import { MatCardModule }    from '@angular/material/card';
import { MatButtonModule }  from '@angular/material/button';
import { MatIconModule }    from '@angular/material/icon';
import { MatInputModule }   from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule }  from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { forkJoin }    from 'rxjs';
import { map }         from 'rxjs/operators';
import { ApiService }         from '../../../core/services/api.service';
import { NominatimService }   from '../../../core/services/nominatim.service';
import { Ride }               from '../../../core/models/ride.model';
import { PageResponse }       from '../../../core/models/profile.model';
import { ParcelSize }         from '../../../core/models/parcel.model';

@Component({
  selector: 'app-post-parcel',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatStepperModule, MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatSelectModule, MatCheckboxModule,
    MatProgressSpinnerModule, MatSnackBarModule
  ],
  templateUrl: './post-parcel.component.html',
  styleUrls:   ['./post-parcel.component.scss']
})
export class PostParcelComponent implements OnInit {

  @ViewChild('stepper') stepper!: MatStepper;

  // ── Step 1: Search & select ride ────────────────────────────────
  searchForm!: FormGroup;
  rides:       Ride[]   = [];
  searching    = false;
  selectedRide: Ride | null = null;

  // ── Step 2: Parcel details ───────────────────────────────────────
  detailsForm!: FormGroup;
  photoFile:   File | null = null;
  photoPreview: string | null = null;

  // ── Submission ───────────────────────────────────────────────────
  submitting = false;

  readonly SIZES: { value: ParcelSize; label: string; hint: string }[] = [
    { value: 'SMALL',  label: 'Small',  hint: 'Fits in a backpack (< 5 kg)' },
    { value: 'MEDIUM', label: 'Medium', hint: 'Carry-on bag size (5–15 kg)' },
    { value: 'LARGE',  label: 'Large',  hint: 'Large box (15–30 kg)' }
  ];

  readonly RESTRICTED_ITEMS = [
    'Weapons, ammunition, or explosives',
    'Drugs or controlled substances',
    'Flammable liquids or gases',
    'Live animals',
    'Cash, jewellery, or valuables exceeding ₹5,000',
    'Stolen or counterfeit goods'
  ];

  constructor(
    private fb:        FormBuilder,
    private api:       ApiService,
    private nominatim: NominatimService,
    private router:    Router,
    private route:     ActivatedRoute,
    private snack:     MatSnackBar
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      from:  ['', Validators.required],
      to:    ['', Validators.required],
      date:  ['', Validators.required]
    });

    this.detailsForm = this.fb.group({
      size:        ['SMALL', Validators.required],
      description: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]],
      price:       [null, [Validators.required, Validators.min(1)]],
      ack:         [false, Validators.requiredTrue]
    });

    // Pre-fill rideId if navigated from ride detail
    const rideId = this.route.snapshot.queryParamMap.get('rideId');
    if (rideId) {
      this.api.getRide(Number(rideId)).subscribe({
        next: r => { this.selectedRide = r; }
      });
    }
  }

  // ── Step 1: search rides ─────────────────────────────────────────

  searchRides(): void {
    if (this.searchForm.invalid) return;
    this.searching = true;
    const { from, to, date } = this.searchForm.value;

    // B1 fix: geocode from/to city names via Nominatim before calling the
    // Haversine ride-search endpoint. Previously sent (0,0) which returned
    // only rides near the Gulf of Guinea, breaking the entire parcel flow.
    forkJoin({
      fromResult: this.nominatim.search(from).pipe(map(r => r[0] ?? null)),
      toResult:   this.nominatim.search(to).pipe(map(r => r[0] ?? null))
    }).subscribe(({ fromResult, toResult }) => {
      if (!fromResult) {
        this.snack.open(`Location not found: "${from}". Try a full city name.`, 'OK', { duration: 4000 });
        this.searching = false;
        return;
      }
      if (!toResult) {
        this.snack.open(`Location not found: "${to}". Try a full city name.`, 'OK', { duration: 4000 });
        this.searching = false;
        return;
      }

      this.api.searchRides({
        fromLat: parseFloat(fromResult.lat),
        fromLng: parseFloat(fromResult.lon),
        toLat:   parseFloat(toResult.lat),
        toLng:   parseFloat(toResult.lon),
        date, seats: 1
      }).subscribe({
        next: (page: PageResponse<Ride>) => {
          this.rides    = page.content.filter(r => r.acceptsParcels);
          this.searching = false;
        },
        error: () => { this.searching = false; }
      });
    });
  }

  selectRide(ride: Ride): void {
    this.selectedRide = ride;
    setTimeout(() => this.stepper.next(), 100);
  }

  // ── Step 2: photo upload ─────────────────────────────────────────

  onPhotoSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) {
      this.snack.open('Photo must be under 5 MB.', 'OK', { duration: 3000 });
      return;
    }
    this.photoFile = file;
    const reader = new FileReader();
    reader.onload = e => this.photoPreview = e.target?.result as string;
    reader.readAsDataURL(file);
  }

  removePhoto(): void {
    this.photoFile    = null;
    this.photoPreview = null;
  }

  // ── Step 3: submit ───────────────────────────────────────────────

  submit(): void {
    if (!this.selectedRide || this.detailsForm.invalid || this.submitting) return;
    this.submitting = true;

    const { size, description, price, ack } = this.detailsForm.value;

    // Build the JSON part of the multipart request
    const req = {
      rideId:   this.selectedRide.id,
      fromLocation: this.selectedRide.fromLocation,
      fromLat:      this.selectedRide.fromLat,
      fromLng:      this.selectedRide.fromLng,
      toLocation:   this.selectedRide.toLocation,
      toLat:        this.selectedRide.toLat,
      toLng:        this.selectedRide.toLng,
      size, description,
      price: Number(price),
      restrictedItemsAcknowledged: ack
    };

    const fd = new FormData();
    fd.append('parcel', new Blob([JSON.stringify(req)], { type: 'application/json' }));
    if (this.photoFile) fd.append('photo', this.photoFile);

    this.api.postParcel(fd).subscribe({
      next: (p) => {
        this.snack.open('Parcel request sent! ✓', 'View', { duration: 5000 })
          .onAction().subscribe(() => this.router.navigate(['/parcels', p.id]));
        this.router.navigate(['/parcels', p.id]);
      },
      error: (err) => {
        this.snack.open(err?.error?.error ?? 'Failed to post parcel.', 'OK', { duration: 4000 });
        this.submitting = false;
      }
    });
  }
}
