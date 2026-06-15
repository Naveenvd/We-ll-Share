import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import {
  SignupRequest, LoginRequest, AuthResponse,
  ApiResponse, SessionUser, UserMode
} from '../models/auth.model';
import { environment } from '../../../environments/environment';

const API = environment.apiUrl;
const SESSION_KEY = 'ridemate_session';

@Injectable({ providedIn: 'root' })
export class AuthService {

  // sessionStorage is tab-isolated: each tab holds its own login session.
  private sessionSubject = new BehaviorSubject<SessionUser | null>(this.loadSession());
  readonly session$ = this.sessionSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ── Auth endpoints ───────────────────────────────────────────────

  signup(payload: SignupRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${API}/auth/signup`, payload);
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API}/auth/login`, payload).pipe(
      tap(res => this.saveSession(res))
    );
  }

  verifyPhone(otp: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${API}/auth/verify-phone`, { otp }).pipe(
      tap(() => {
        const session = this.sessionSubject.value;
        if (session) {
          session.phoneVerified = true;
          this.saveSession(session);
        }
      })
    );
  }

  resendOtp(): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${API}/auth/resend-otp`, {});
  }

  forgotPassword(email: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${API}/auth/forgot-password`, { email });
  }

  resetPassword(email: string, otp: string, newPassword: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${API}/auth/reset-password`, { email, otp, newPassword });
  }

  logout(): void {
    sessionStorage.removeItem(SESSION_KEY);
    this.sessionSubject.next(null);
  }

  // ── Mode helpers ─────────────────────────────────────────────────

  /** Save the user's chosen mode (RIDER / DRIVER) into the session */
  setUserMode(mode: UserMode): void {
    const session = this.sessionSubject.value;
    if (session) {
      const updated: SessionUser = { ...session, userMode: mode };
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(updated));
      this.sessionSubject.next(updated);
    }
  }

  getUserMode(): UserMode | undefined {
    return this.sessionSubject.value?.userMode;
  }

  // ── Session helpers ─────────────────────────────────────────────

  getToken(): string | null {
    return this.sessionSubject.value?.token ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.sessionSubject.value;
  }

  isAdmin(): boolean {
    return this.sessionSubject.value?.role === 'ADMIN';
  }

  getCurrentSession(): SessionUser | null {
    return this.sessionSubject.value;
  }

  getSession(): SessionUser | null {
    return this.sessionSubject.value;
  }

  private saveSession(data: AuthResponse | SessionUser): void {
    const existing = this.sessionSubject.value;
    const session: SessionUser = {
      token:         (data as AuthResponse).token ?? (data as SessionUser).token,
      userId:        data.userId,
      name:          data.name,
      email:         data.email,
      role:          data.role,
      status:        data.status,
      phoneVerified: data.phoneVerified,
      // preserve existing userMode if re-saving (e.g. phone verify)
      userMode:      (data as SessionUser).userMode ?? existing?.userMode
    };
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
    this.sessionSubject.next(session);
  }

  private loadSession(): SessionUser | null {
    try {
      const raw = sessionStorage.getItem(SESSION_KEY);
      return raw ? (JSON.parse(raw) as SessionUser) : null;
    } catch {
      return null;
    }
  }
}
