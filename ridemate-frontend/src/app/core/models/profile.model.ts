import { Gender, Role, UserStatus } from './auth.model';

export interface UserProfile {
  id: number;
  name: string;
  email: string;
  phone: string;
  gender: Gender;
  dob: string;
  role: Role;
  status: UserStatus;
  rejectionReason?: string;
  photoUrl?: string;
  aadhaarNumber?: string;
  aadhaarDocUrl?: string;
  dlNumber?: string;
  dlDocUrl?: string;
  phoneVerified: boolean;
  avgRating: number;
  totalRides: number;
  totalParcelsDelivered: number;
  createdAt: string;
}

export interface Vehicle {
  id: number;
  model: string;
  numberPlate: string;
  color: string;
  seats: number;
  createdAt: string;
}

export interface VehicleRequest {
  model: string;
  numberPlate: string;
  color: string;
  seats: number;
}

export interface EmergencyContact {
  id: number;
  name: string;
  phone: string;
  relation: string;
}

export interface EmergencyContactRequest {
  name: string;
  phone: string;
  relation: string;
}

// ── Admin types ─────────────────────────────────────────────────────

export interface AdminUserSummary {
  id: number;
  name: string;
  email: string;
  phone: string;
  gender: Gender;
  status: UserStatus;
  rejectionReason?: string;
  phoneVerified: boolean;
  aadhaarNumber?: string;
  aadhaarDocUrl?: string;
  dlNumber?: string;
  dlDocUrl?: string;
  photoUrl?: string;
  createdAt: string;
}

export interface AdminDashboardStats {
  totalUsers: number;
  pendingVerifications: number;
  verifiedUsers: number;
  totalRides: number;
  totalParcels: number;
  openSosAlerts: number;
  openDisputes: number;
  openReports: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
