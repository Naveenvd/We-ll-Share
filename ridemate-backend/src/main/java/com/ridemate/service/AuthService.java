package com.ridemate.service;

import com.ridemate.dto.request.*;
import com.ridemate.dto.response.*;
import com.ridemate.entity.User;
import com.ridemate.enums.OtpType;
import com.ridemate.enums.UserStatus;
import com.ridemate.exception.AppException;
import com.ridemate.repository.UserRepository;
import com.ridemate.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    /**
     * Registers a new user.
     * Phone OTP is sent immediately after registration.
     */
    @Transactional
    public ApiResponse signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException("Email is already registered.", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByPhone(req.getPhone())) {
            throw new AppException("Phone number is already registered.", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .gender(req.getGender())
                .dob(req.getDob())
                .build();

        userRepository.save(user);

        // Send phone verification OTP (mocked — printed to console)
        otpService.generateAndSend(user, OtpType.PHONE);

        return ApiResponse.ok("Registration successful. Please verify your phone number with the OTP sent.");
    }

    /**
     * Verifies the phone OTP sent at signup.
     * Marks phone as verified; account still in PENDING_VERIFICATION until admin approves.
     */
    @Transactional
    public ApiResponse verifyPhone(String email, OtpVerifyRequest req) {
        User user = findByEmail(email);
        if (user.isPhoneVerified()) {
            return ApiResponse.ok("Phone is already verified.");
        }
        otpService.verify(user, OtpType.PHONE, req.getOtp());
        user.setPhoneVerified(true);
        userRepository.save(user);
        return ApiResponse.ok("Phone verified successfully. Your account is pending admin verification.");
    }

    /**
     * Resends the phone OTP.
     */
    public ApiResponse resendPhoneOtp(String email) {
        User user = findByEmail(email);
        otpService.generateAndSend(user, OtpType.PHONE);
        return ApiResponse.ok("OTP resent.");
    }

    /**
     * Authenticates a user and returns a JWT.
     */
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException("Invalid credentials.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new AppException("Invalid credentials.", HttpStatus.UNAUTHORIZED);
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AppException("Your account has been suspended. Contact support.", HttpStatus.FORBIDDEN);
        }
        // B3 fix: prevent REJECTED users from obtaining a JWT
        if (user.getStatus() == UserStatus.REJECTED) {
            String reason = user.getRejectionReason() != null
                ? " Reason: " + user.getRejectionReason()
                : " Please contact support.";
            throw new AppException("Your account was rejected." + reason, HttpStatus.FORBIDDEN);
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .phoneVerified(user.isPhoneVerified())
                .build();
    }

    /**
     * Initiates password reset — sends OTP to the user's email (mocked via console).
     */
    public ApiResponse forgotPassword(ForgotPasswordRequest req) {
        User user = findByEmail(req.getEmail());
        otpService.generateAndSend(user, OtpType.PASSWORD_RESET);
        return ApiResponse.ok("Password reset OTP sent to your registered phone/email.");
    }

    /**
     * Completes password reset after OTP verification.
     */
    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest req) {
        User user = findByEmail(req.getEmail());
        otpService.verify(user, OtpType.PASSWORD_RESET, req.getOtp());
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.ok("Password reset successfully. You can now log in.");
    }

    // ── Internal helpers ──────────────────────────────────────────────

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }
}
