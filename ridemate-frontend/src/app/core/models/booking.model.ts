import { Ride } from './ride.model';

// ── Enums ──────────────────────────────────────────────────────────────────

export type BookingStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'STARTED'
  | 'COMPLETED';

// ── DTOs ───────────────────────────────────────────────────────────────────

export interface PassengerSummary {
  id: number;
  name: string;
  photoUrl: string | null;
  gender: string;
  avgRating: number;
  totalRides: number;
}

export interface Booking {
  id: number;
  ride: Ride;
  passenger: PassengerSummary;
  seatsBooked: number;
  status: BookingStatus;
  /** Only visible to the passenger once the ride is STARTED */
  tripOtp: string | null;
  otpVerified: boolean;
  /** UUID token for public share link */
  tripShareToken: string | null;
  amount: number;
  createdAt: string;
  unreadMessages: number;
}

export interface BookingRequest {
  rideId: number;
  seatsBooked: number;
}

export interface TripOtpRequest {
  otp: string;
}

// ── Chat ───────────────────────────────────────────────────────────────────

export interface ChatMessage {
  id: number;
  bookingId: number | null;
  parcelId:  number | null;   // I3 fix: was missing; populated for parcel chat messages
  senderId: number;
  senderName: string;
  senderPhotoUrl: string | null;
  text: string;
  read: boolean;
  sentAt: string;
}

export interface ChatMessageRequest {
  text: string;
}
