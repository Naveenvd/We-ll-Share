package com.ridemate.controller;

import com.ridemate.dto.request.*;
import com.ridemate.dto.response.*;
import com.ridemate.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    /**
     * TEMPORARY — remove after first use.
     * GET /api/auth/hash?pw=yourPassword  → returns the BCrypt hash
     */
    @GetMapping("/hash")
    public ResponseEntity<String> hash(@RequestParam String pw) {
        return ResponseEntity.ok(passwordEncoder.encode(pw));
    }

    /** POST /api/auth/signup */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.ok(authService.signup(req));
    }

    /** POST /api/auth/login */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /**
     * POST /api/auth/verify-phone
     * Requires a valid JWT — user must be logged in to verify their own phone.
     */
    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse> verifyPhone(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody OtpVerifyRequest req) {
        return ResponseEntity.ok(authService.verifyPhone(principal.getUsername(), req));
    }

    /** POST /api/auth/resend-otp */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(authService.resendPhoneOtp(principal.getUsername()));
    }

    /** POST /api/auth/forgot-password */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.ok(authService.forgotPassword(req));
    }

    /** POST /api/auth/reset-password */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {
        return ResponseEntity.ok(authService.resetPassword(req));
    }
}
