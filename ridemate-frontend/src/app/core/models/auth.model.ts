export type Gender = 'MALE' | 'FEMALE' | 'OTHER';
export type Role = 'USER' | 'ADMIN';
export type UserStatus = 'PENDING_VERIFICATION' | 'VERIFIED' | 'REJECTED' | 'SUSPENDED';
export type UserMode = 'RIDER' | 'DRIVER';

export interface SignupRequest {
  name: string;
  email: string;
  phone: string;
  password: string;
  gender: Gender;
  dob: string; // ISO date: "1995-06-15"
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: Role;
  status: UserStatus;
  phoneVerified: boolean;
}

export interface ApiResponse {
  success: boolean;
  message: string;
}

/** Shape stored in sessionStorage after login */
export interface SessionUser {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: Role;
  status: UserStatus;
  phoneVerified: boolean;
  userMode?: UserMode;   // set on role-select page after first login
}
