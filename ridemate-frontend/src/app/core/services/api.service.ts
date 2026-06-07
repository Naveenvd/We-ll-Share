import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  UserProfile, Vehicle, VehicleRequest,
  EmergencyContact, EmergencyContactRequest,
  AdminUserSummary, AdminDashboardStats, PageResponse
} from '../models/profile.model';
import { ApiResponse } from '../models/auth.model';
import { Ride, RidePostRequest, RideSearchParams } from '../models/ride.model';
import { Booking, BookingRequest, ChatMessage } from '../models/booking.model';
import { Parcel, ParcelPostRequest, ParcelComplaint } from '../models/parcel.model';
import {
  SosRequest, SosAlertResponse,
  ReportRequest, ReportResponse,
  BlockedUserResponse, HistoryItem
} from '../models/safety.model';
import { ReviewRequest, ReviewResponse } from '../models/review.model';

const API = 'http://localhost:8081/api';

@Injectable({ providedIn: 'root' })
export class ApiService {

  constructor(private http: HttpClient) {}

  // ── Profile ─────────────────────────────────────────────────────
  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${API}/profile`);
  }

  updateProfile(data: { name: string; gender: string; dob: string }): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${API}/profile`, data);
  }

  uploadPhoto(file: File): Observable<UserProfile> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<UserProfile>(`${API}/profile/photo`, fd);
  }

  uploadAadhaar(aadhaarNumber: string, file: File): Observable<UserProfile> {
    const fd = new FormData();
    fd.append('aadhaarNumber', aadhaarNumber);
    fd.append('file', file);
    return this.http.post<UserProfile>(`${API}/profile/aadhaar`, fd);
  }

  uploadDl(dlNumber: string, file: File): Observable<UserProfile> {
    const fd = new FormData();
    fd.append('dlNumber', dlNumber);
    fd.append('file', file);
    return this.http.post<UserProfile>(`${API}/profile/dl`, fd);
  }

  // ── Vehicles ────────────────────────────────────────────────────
  getVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(`${API}/profile/vehicles`);
  }

  addVehicle(req: VehicleRequest): Observable<Vehicle> {
    return this.http.post<Vehicle>(`${API}/profile/vehicles`, req);
  }

  updateVehicle(id: number, req: VehicleRequest): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${API}/profile/vehicles/${id}`, req);
  }

  deleteVehicle(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API}/profile/vehicles/${id}`);
  }

  // ── Emergency Contacts ──────────────────────────────────────────
  getEmergencyContacts(): Observable<EmergencyContact[]> {
    return this.http.get<EmergencyContact[]>(`${API}/profile/emergency-contacts`);
  }

  addEmergencyContact(req: EmergencyContactRequest): Observable<EmergencyContact> {
    return this.http.post<EmergencyContact>(`${API}/profile/emergency-contacts`, req);
  }

  updateEmergencyContact(id: number, req: EmergencyContactRequest): Observable<EmergencyContact> {
    return this.http.put<EmergencyContact>(`${API}/profile/emergency-contacts/${id}`, req);
  }

  deleteEmergencyContact(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API}/profile/emergency-contacts/${id}`);
  }

  // ── Admin ────────────────────────────────────────────────────────
  getAdminStats(): Observable<AdminDashboardStats> {
    return this.http.get<AdminDashboardStats>(`${API}/admin/stats`);
  }

  getPendingVerifications(page = 0, size = 20): Observable<PageResponse<AdminUserSummary>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AdminUserSummary>>(`${API}/admin/verifications`, { params });
  }

  verifyUser(userId: number, approve: boolean, rejectionReason?: string): Observable<AdminUserSummary> {
    return this.http.post<AdminUserSummary>(`${API}/admin/verifications/${userId}`,
      { approve, rejectionReason });
  }

  listAdminUsers(query: string, page = 0, size = 20): Observable<PageResponse<AdminUserSummary>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (query) params = params.set('query', query);
    return this.http.get<PageResponse<AdminUserSummary>>(`${API}/admin/users`, { params });
  }

  suspendUser(userId: number): Observable<AdminUserSummary> {
    return this.http.post<AdminUserSummary>(`${API}/admin/users/${userId}/suspend`, {});
  }

  unblockUser(userId: number): Observable<AdminUserSummary> {
    return this.http.post<AdminUserSummary>(`${API}/admin/users/${userId}/unblock`, {});
  }

  // ── Rides ────────────────────────────────────────────────────────
  postRide(req: RidePostRequest): Observable<Ride> {
    return this.http.post<Ride>(`${API}/rides`, req);
  }

  getMyRides(): Observable<Ride[]> {
    return this.http.get<Ride[]>(`${API}/rides/my`);
  }

  getRide(id: number): Observable<Ride> {
    return this.http.get<Ride>(`${API}/rides/${id}`);
  }

  cancelRide(id: number): Observable<Ride> {
    return this.http.delete<Ride>(`${API}/rides/${id}`);
  }

  searchRides(params: RideSearchParams): Observable<PageResponse<Ride>> {
    let p = new HttpParams()
      .set('fromLat', params.fromLat).set('fromLng', params.fromLng)
      .set('toLat',   params.toLat)  .set('toLng',   params.toLng)
      .set('date',    params.date)
      .set('seats',   params.seats)
      .set('page',    params.page  ?? 0)
      .set('size',    params.size  ?? 20);
    if (params.minPrice !== undefined) p = p.set('minPrice', params.minPrice);
    if (params.maxPrice !== undefined) p = p.set('maxPrice', params.maxPrice);
    if (params.womenOnly !== undefined) p = p.set('womenOnly', params.womenOnly);
    return this.http.get<PageResponse<Ride>>(`${API}/rides/search`, { params: p });
  }

  // ── Bookings ─────────────────────────────────────────────────────

  requestBooking(req: BookingRequest): Observable<Booking> {
    return this.http.post<Booking>(`${API}/bookings`, req);
  }

  getMyBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${API}/bookings/my`);
  }

  getDriverBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${API}/bookings/driver`);
  }

  getPendingDriverBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${API}/bookings/driver/pending`);
  }

  getBooking(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${API}/bookings/${id}`);
  }

  approveBooking(id: number): Observable<Booking> {
    return this.http.post<Booking>(`${API}/bookings/${id}/approve`, {});
  }

  rejectBooking(id: number): Observable<Booking> {
    return this.http.post<Booking>(`${API}/bookings/${id}/reject`, {});
  }

  cancelBooking(id: number): Observable<Booking> {
    return this.http.post<Booking>(`${API}/bookings/${id}/cancel`, {});
  }

  startRide(rideId: number): Observable<Booking[]> {
    return this.http.post<Booking[]>(`${API}/rides/${rideId}/start`, {});
  }

  verifyTripOtp(bookingId: number, otp: string): Observable<Booking> {
    return this.http.post<Booking>(`${API}/bookings/${bookingId}/verify-otp`, { otp });
  }

  completeBooking(id: number): Observable<Booking> {
    return this.http.post<Booking>(`${API}/bookings/${id}/complete`, {});
  }

  getBookingByShareToken(token: string): Observable<Booking> {
    return this.http.get<Booking>(`${API}/public/track/${token}`);
  }

  // ── Chat ─────────────────────────────────────────────────────────

  getChatHistory(bookingId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${API}/chat/booking/${bookingId}`);
  }

  markChatRead(bookingId: number): Observable<void> {
    return this.http.post<void>(`${API}/chat/booking/${bookingId}/read`, {});
  }

  getParcelChatHistory(parcelId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${API}/chat/parcel/${parcelId}`);
  }

  markParcelChatRead(parcelId: number): Observable<void> {
    return this.http.post<void>(`${API}/chat/parcel/${parcelId}/read`, {});
  }

  // ── Parcels ──────────────────────────────────────────────────────

  /**
   * Post a parcel — multipart: JSON part "parcel" + optional file "photo".
   * Caller must build FormData manually.
   */
  postParcel(formData: FormData): Observable<Parcel> {
    return this.http.post<Parcel>(`${API}/parcels`, formData);
  }

  getMySentParcels(): Observable<Parcel[]> {
    return this.http.get<Parcel[]>(`${API}/parcels/my`);
  }

  getDriverParcels(): Observable<Parcel[]> {
    return this.http.get<Parcel[]>(`${API}/parcels/driver`);
  }

  getPendingDriverParcels(): Observable<Parcel[]> {
    return this.http.get<Parcel[]>(`${API}/parcels/driver/pending`);
  }

  getParcelsForRide(rideId: number): Observable<Parcel[]> {
    return this.http.get<Parcel[]>(`${API}/parcels/ride/${rideId}`);
  }

  getParcel(id: number): Observable<Parcel> {
    return this.http.get<Parcel>(`${API}/parcels/${id}`);
  }

  acceptParcel(id: number): Observable<Parcel> {
    return this.http.post<Parcel>(`${API}/parcels/${id}/accept`, {});
  }

  rejectParcel(id: number): Observable<Parcel> {
    return this.http.post<Parcel>(`${API}/parcels/${id}/reject`, {});
  }

  cancelParcel(id: number): Observable<Parcel> {
    return this.http.post<Parcel>(`${API}/parcels/${id}/cancel`, {});
  }

  verifyPickupOtp(id: number, otp: string, beforePhoto?: File): Observable<Parcel> {
    const fd = new FormData();
    fd.append('otp', otp);
    if (beforePhoto) fd.append('beforePhoto', beforePhoto);
    return this.http.post<Parcel>(`${API}/parcels/${id}/pickup`, fd);
  }

  verifyDeliveryOtp(id: number, otp: string, afterPhoto?: File): Observable<Parcel> {
    const fd = new FormData();
    fd.append('otp', otp);
    if (afterPhoto) fd.append('afterPhoto', afterPhoto);
    return this.http.post<Parcel>(`${API}/parcels/${id}/deliver`, fd);
  }

  raiseParcelComplaint(id: number, reason: string): Observable<ParcelComplaint> {
    return this.http.post<ParcelComplaint>(`${API}/parcels/${id}/complaint`, { reason });
  }

  // ── SOS ──────────────────────────────────────────────────────────

  triggerSos(req: SosRequest): Observable<SosAlertResponse> {
    return this.http.post<SosAlertResponse>(`${API}/sos`, req);
  }

  // ── Reports ──────────────────────────────────────────────────────

  reportUser(req: ReportRequest): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(`${API}/reports`, req);
  }

  // ── Block / Unblock ──────────────────────────────────────────────

  blockUser(userId: number): Observable<BlockedUserResponse> {
    return this.http.post<BlockedUserResponse>(`${API}/blocked-users/${userId}`, {});
  }

  unblockUserSafety(userId: number): Observable<void> {
    return this.http.delete<void>(`${API}/blocked-users/${userId}`);
  }

  getBlockedUsers(): Observable<BlockedUserResponse[]> {
    return this.http.get<BlockedUserResponse[]>(`${API}/blocked-users`);
  }

  // ── History ──────────────────────────────────────────────────────

  getHistory(): Observable<HistoryItem[]> {
    return this.http.get<HistoryItem[]>(`${API}/history`);
  }

  // ── Reviews ──────────────────────────────────────────────────────

  submitReview(req: ReviewRequest): Observable<ReviewResponse> {
    return this.http.post<ReviewResponse>(`${API}/reviews`, req);
  }

  getReviewsForUser(userId: number): Observable<ReviewResponse[]> {
    return this.http.get<ReviewResponse[]>(`${API}/reviews/user/${userId}`);
  }

  getMyReviews(): Observable<ReviewResponse[]> {
    return this.http.get<ReviewResponse[]>(`${API}/reviews/my`);
  }

  // ── Admin — SOS ──────────────────────────────────────────────────

  getAdminOpenSos(): Observable<SosAlertResponse[]> {
    return this.http.get<SosAlertResponse[]>(`${API}/admin/sos`);
  }

  acknowledgeAdminSos(alertId: number): Observable<SosAlertResponse> {
    return this.http.post<SosAlertResponse>(`${API}/admin/sos/${alertId}/acknowledge`, {});
  }

  // ── Admin — Reports ──────────────────────────────────────────────

  getAdminOpenReports(): Observable<ReportResponse[]> {
    return this.http.get<ReportResponse[]>(`${API}/admin/reports`);
  }

  resolveAdminReport(reportId: number, resolution: string): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(
      `${API}/admin/reports/${reportId}/resolve`, { resolution });
  }
}
