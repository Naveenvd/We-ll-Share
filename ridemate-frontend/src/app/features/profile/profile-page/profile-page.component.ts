import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { ApiService } from '../../../core/services/api.service';
import {
  UserProfile, Vehicle, VehicleRequest,
  EmergencyContact, EmergencyContactRequest
} from '../../../core/models/profile.model';
import { BlockedUserResponse } from '../../../core/models/safety.model';
import { ReviewResponse }      from '../../../core/models/review.model';
import { VehicleDialogComponent } from '../dialogs/vehicle-dialog.component';
import { EmergencyContactDialogComponent } from '../dialogs/emergency-contact-dialog.component';
import {
  ConfirmDialogComponent, ConfirmDialogData
} from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatTabsModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatSelectModule, MatDatepickerModule,
    MatNativeDateModule, MatProgressSpinnerModule, MatSnackBarModule,
    MatChipsModule, MatDividerModule, MatDialogModule, MatProgressBarModule
  ],
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.scss']
})
export class ProfilePageComponent implements OnInit {

  profile:        UserProfile | null = null;
  vehicles:       Vehicle[] = [];
  contacts:       EmergencyContact[] = [];
  blockedUsers:   BlockedUserResponse[] = [];
  reviews:        ReviewResponse[] = [];
  loadingReviews  = false;

  loadingProfile   = true;
  savingProfile    = false;
  uploadingPhoto   = false;
  uploadingAadhaar = false;
  uploadingDl      = false;

  maxDob = new Date();

  /** Profile edit form */
  profileForm = this.fb.group({
    name:   ['', [Validators.required, Validators.minLength(2)]],
    gender: ['', Validators.required],
    dob:    [null as Date | null, Validators.required]
  });

  /** Aadhaar upload form */
  aadhaarForm = this.fb.group({
    aadhaarNumber: ['', [Validators.required, Validators.pattern(/^\d{12}$/)]]
  });
  aadhaarFile: File | null = null;

  /** DL upload form */
  dlForm = this.fb.group({
    dlNumber: ['', [Validators.required, Validators.minLength(10)]]
  });
  dlFile: File | null = null;

  constructor(
    private api: ApiService,
    private fb: FormBuilder,
    private snack: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  // ── Load ──────────────────────────────────────────────────────

  loadAll(): void {
    this.loadingProfile = true;
    this.api.getProfile().subscribe({
      next: (p) => {
        this.profile = p;
        this.profileForm.patchValue({
          name:   p.name,
          gender: p.gender,
          dob:    p.dob ? new Date(p.dob) : null
        });
        this.loadingProfile = false;

        // B10 fix: load reviews using the already-fetched profile — no second getProfile() call
        this.loadingReviews = true;
        this.api.getReviewsForUser(p.id).subscribe({
          next: r => { this.reviews = r; this.loadingReviews = false; },
          error: () => { this.loadingReviews = false; }
        });
      },
      error: () => { this.loadingProfile = false; }
    });

    this.api.getVehicles().subscribe(v => this.vehicles = v);
    this.api.getEmergencyContacts().subscribe(c => this.contacts = c);
    this.api.getBlockedUsers().subscribe(b => this.blockedUsers = b);
  }

  // ── Profile save ──────────────────────────────────────────────

  saveProfile(): void {
    if (this.profileForm.invalid) return;
    this.savingProfile = true;
    const d = this.profileForm.value.dob as Date;
    this.api.updateProfile({
      name:   this.profileForm.value.name!,
      gender: this.profileForm.value.gender!,
      dob:    this.formatDate(d)
    }).subscribe({
      next: (p) => {
        this.profile = p;
        this.savingProfile = false;
        this.snack.open('Profile updated.', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.savingProfile = false;
        this.snack.open(err.error?.error || 'Update failed.', 'Close', { duration: 3000 });
      }
    });
  }

  // ── Photo upload ──────────────────────────────────────────────

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file) return;
    this.uploadingPhoto = true;
    this.api.uploadPhoto(file).subscribe({
      next: (p) => {
        this.profile = p;
        this.uploadingPhoto = false;
        this.snack.open('Photo updated.', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.uploadingPhoto = false;
        this.snack.open(err.error?.error || 'Photo upload failed.', 'Close', { duration: 3000 });
      }
    });
  }

  // ── Aadhaar upload ────────────────────────────────────────────

  onAadhaarFileSelected(event: Event): void {
    this.aadhaarFile = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  submitAadhaar(): void {
    if (this.aadhaarForm.invalid || !this.aadhaarFile) return;
    this.uploadingAadhaar = true;
    this.api.uploadAadhaar(this.aadhaarForm.value.aadhaarNumber!, this.aadhaarFile).subscribe({
      next: (p) => {
        this.profile = p;
        this.uploadingAadhaar = false;
        this.aadhaarFile = null;
        this.snack.open('Aadhaar uploaded. Pending admin review.', 'OK', { duration: 4000 });
      },
      error: (err) => {
        this.uploadingAadhaar = false;
        this.snack.open(err.error?.error || 'Upload failed.', 'Close', { duration: 3000 });
      }
    });
  }

  // ── DL upload ─────────────────────────────────────────────────

  onDlFileSelected(event: Event): void {
    this.dlFile = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  submitDl(): void {
    if (this.dlForm.invalid || !this.dlFile) return;
    this.uploadingDl = true;
    this.api.uploadDl(this.dlForm.value.dlNumber!, this.dlFile).subscribe({
      next: (p) => {
        this.profile = p;
        this.uploadingDl = false;
        this.dlFile = null;
        this.snack.open('Driving licence uploaded. Pending admin review.', 'OK', { duration: 4000 });
      },
      error: (err) => {
        this.uploadingDl = false;
        this.snack.open(err.error?.error || 'Upload failed.', 'Close', { duration: 3000 });
      }
    });
  }

  // ── Vehicle dialogs ───────────────────────────────────────────

  openAddVehicle(): void {
    const ref = this.dialog.open(VehicleDialogComponent, { width: '420px' });
    ref.afterClosed().subscribe((req: VehicleRequest | undefined) => {
      if (!req) return;
      this.api.addVehicle(req).subscribe({
        next: (v) => {
          this.vehicles.push(v);
          this.snack.open('Vehicle added.', 'OK', { duration: 3000 });
        },
        error: (err) => this.snack.open(err.error?.error || 'Failed.', 'Close', { duration: 3000 })
      });
    });
  }

  openEditVehicle(v: Vehicle): void {
    const ref = this.dialog.open(VehicleDialogComponent, { width: '420px', data: v });
    ref.afterClosed().subscribe((req: VehicleRequest | undefined) => {
      if (!req) return;
      this.api.updateVehicle(v.id, req).subscribe({
        next: (updated) => {
          this.vehicles = this.vehicles.map(x => x.id === updated.id ? updated : x);
          this.snack.open('Vehicle updated.', 'OK', { duration: 3000 });
        },
        error: (err) => this.snack.open(err.error?.error || 'Failed.', 'Close', { duration: 3000 })
      });
    });
  }

  deleteVehicle(v: Vehicle): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        `Remove ${v.model}`,
        message:      `Remove ${v.numberPlate} from your vehicles? This cannot be undone.`,
        confirmLabel: 'Remove'
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.api.deleteVehicle(v.id).subscribe({
        next: () => {
          this.vehicles = this.vehicles.filter(x => x.id !== v.id);
          this.snack.open('Vehicle removed.', 'OK', { duration: 3000 });
        }
      });
    });
  }

  // ── Emergency contact dialogs ─────────────────────────────────

  openAddContact(): void {
    const ref = this.dialog.open(EmergencyContactDialogComponent, { width: '420px' });
    ref.afterClosed().subscribe((req: EmergencyContactRequest | undefined) => {
      if (!req) return;
      this.api.addEmergencyContact(req).subscribe({
        next: (c) => {
          this.contacts.push(c);
          this.snack.open('Contact added.', 'OK', { duration: 3000 });
        },
        error: (err) => this.snack.open(err.error?.error || 'Failed.', 'Close', { duration: 3000 })
      });
    });
  }

  openEditContact(c: EmergencyContact): void {
    const ref = this.dialog.open(EmergencyContactDialogComponent, { width: '420px', data: c });
    ref.afterClosed().subscribe((req: EmergencyContactRequest | undefined) => {
      if (!req) return;
      this.api.updateEmergencyContact(c.id, req).subscribe({
        next: (updated) => {
          this.contacts = this.contacts.map(x => x.id === updated.id ? updated : x);
          this.snack.open('Contact updated.', 'OK', { duration: 3000 });
        },
        error: (err) => this.snack.open(err.error?.error || 'Failed.', 'Close', { duration: 3000 })
      });
    });
  }

  deleteContact(c: EmergencyContact): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        `Remove ${c.name}`,
        message:      `Remove ${c.name} from your emergency contacts?`,
        confirmLabel: 'Remove'
      }
    });
    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.api.deleteEmergencyContact(c.id).subscribe({
        next: () => {
          this.contacts = this.contacts.filter(x => x.id !== c.id);
          this.snack.open('Contact removed.', 'OK', { duration: 3000 });
        }
      });
    });
  }

  // ── Blocked users ─────────────────────────────────────────────

  unblockUser(entry: BlockedUserResponse): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: <ConfirmDialogData>{
        title:        `Unblock ${entry.blockedUserName}`,
        message:      `${entry.blockedUserName} will be able to message you again.`,
        confirmLabel: 'Unblock',
        confirmColor: 'primary'
      }
    });
    ref.afterClosed().subscribe(confirmed => { if (!confirmed) return; this._doUnblock(entry); });
  }

  private _doUnblock(entry: BlockedUserResponse): void {
    this.api.unblockUserSafety(entry.blockedUserId).subscribe({
      next: () => {
        this.blockedUsers = this.blockedUsers.filter(b => b.id !== entry.id);
        this.snack.open(`${entry.blockedUserName} has been unblocked.`, 'OK', { duration: 3000 });
      },
      error: (err) => this.snack.open(err.error?.error || 'Failed to unblock.', 'Close', { duration: 3000 })
    });
  }

  // ── Helpers ───────────────────────────────────────────────────

  get fileName(): string { return this.aadhaarFile?.name ?? 'No file chosen'; }
  get dlFileName(): string { return this.dlFile?.name ?? 'No file chosen'; }

  private formatDate(d: Date): string {
    const y   = d.getFullYear();
    const m   = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}
