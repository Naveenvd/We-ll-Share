package com.ridemate.controller;

import com.ridemate.dto.request.BookingRequest;
import com.ridemate.dto.request.TripOtpRequest;
import com.ridemate.dto.response.BookingResponse;
import com.ridemate.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ── Passenger: request a booking ──────────────────────────────────

    @PostMapping("/api/bookings")
    public ResponseEntity<BookingResponse> requestBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookingRequest req) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(bookingService.requestBooking(userDetails.getUsername(), req));
    }

    // ── Passenger: view own bookings ──────────────────────────────────

    @GetMapping("/api/bookings/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(bookingService.getMyBookings(userDetails.getUsername()));
    }

    // ── Passenger: cancel a booking ───────────────────────────────────

    @PostMapping("/api/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(
            bookingService.cancelBooking(id, userDetails.getUsername()));
    }

    // ── Driver: view all bookings for their rides ─────────────────────

    @GetMapping("/api/bookings/driver")
    public ResponseEntity<List<BookingResponse>> getDriverBookings(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(bookingService.getDriverBookings(userDetails.getUsername()));
    }

    @GetMapping("/api/bookings/driver/pending")
    public ResponseEntity<List<BookingResponse>> getPendingDriverBookings(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(bookingService.getPendingDriverBookings(userDetails.getUsername()));
    }

    // ── Driver: approve / reject ──────────────────────────────────────

    @PostMapping("/api/bookings/{id}/approve")
    public ResponseEntity<BookingResponse> approveBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(
            bookingService.approveBooking(id, userDetails.getUsername()));
    }

    @PostMapping("/api/bookings/{id}/reject")
    public ResponseEntity<BookingResponse> rejectBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(
            bookingService.rejectBooking(id, userDetails.getUsername()));
    }

    // ── Driver: start ride (generates per-booking OTPs) ───────────────

    @PostMapping("/api/rides/{rideId}/start")
    public ResponseEntity<List<BookingResponse>> startRide(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long rideId) {

        return ResponseEntity.ok(
            bookingService.startRide(rideId, userDetails.getUsername()));
    }

    // ── Driver: verify passenger OTP at pickup ────────────────────────

    @PostMapping("/api/bookings/{id}/verify-otp")
    public ResponseEntity<BookingResponse> verifyTripOtp(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TripOtpRequest req) {

        return ResponseEntity.ok(
            bookingService.verifyTripOtp(id, userDetails.getUsername(), req.getOtp()));
    }

    // ── Driver: mark booking COMPLETED ───────────────────────────────

    @PostMapping("/api/bookings/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(
            bookingService.completeBooking(id, userDetails.getUsername()));
    }

    // ── Any authenticated party: single booking ───────────────────────

    @GetMapping("/api/bookings/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(
            bookingService.getBooking(id, userDetails.getUsername()));
    }

    // ── Public: trip-share link (no auth) ─────────────────────────────

    @GetMapping("/api/public/track/{token}")
    public ResponseEntity<BookingResponse> getBookingByShareToken(
            @PathVariable String token) {

        return ResponseEntity.ok(bookingService.getBookingByShareToken(token));
    }
}
