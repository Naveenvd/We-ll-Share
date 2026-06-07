import { Ride } from './ride.model';
import { PassengerSummary } from './booking.model';

// ── Enums ──────────────────────────────────────────────────────────────────

export type ParcelSize   = 'SMALL' | 'MEDIUM' | 'LARGE';
export type ParcelStatus =
  | 'PENDING'
  | 'ACCEPTED'
  | 'IN_TRANSIT'
  | 'DELIVERED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'COMPLAINT_RAISED';

// ── Parcel ─────────────────────────────────────────────────────────────────

export interface Parcel {
  id: number;
  sender: PassengerSummary;
  ride: Ride;

  fromLocation: string;
  fromLat: number;
  fromLng: number;
  toLocation: string;
  toLat: number;
  toLng: number;

  size: ParcelSize;
  description: string;
  photoUrl: string | null;
  price: number;
  restrictedItemsAcknowledged: boolean;

  status: ParcelStatus;

  /** Shown to sender when status is ACCEPTED or IN_TRANSIT */
  pickupOtp: string | null;
  pickupOtpVerified: boolean;
  beforePhotoUrl: string | null;

  /** Shown to sender when status is IN_TRANSIT */
  deliveryOtp: string | null;
  deliveryOtpVerified: boolean;
  afterPhotoUrl: string | null;

  createdAt: string;
  unreadMessages: number;
}

// ── Request ────────────────────────────────────────────────────────────────

export interface ParcelPostRequest {
  rideId: number;
  fromLocation: string;
  fromLat: number;
  fromLng: number;
  toLocation: string;
  toLat: number;
  toLng: number;
  size: ParcelSize;
  description: string;
  price: number;
  restrictedItemsAcknowledged: boolean;
}

export interface ParcelComplaintRequest {
  reason: string;
}

// ── Complaint ──────────────────────────────────────────────────────────────

export interface ParcelComplaint {
  id: number;
  parcelId: number;
  raisedById: number;
  raisedByName: string;
  reason: string;
  resolution: string | null;
  createdAt: string;
}
