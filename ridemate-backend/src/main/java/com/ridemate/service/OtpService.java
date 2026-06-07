package com.ridemate.service;

import com.ridemate.entity.OtpCode;
import com.ridemate.entity.User;
import com.ridemate.enums.OtpType;
import com.ridemate.exception.AppException;
import com.ridemate.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;

    @Value("${app.otp.expiry-minutes}")
    private int expiryMinutes;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a 6-digit OTP, invalidates previous ones of the same type,
     * persists the new one, and prints it to console (mock SMS/email).
     */
    @Transactional
    public String generateAndSend(User user, OtpType type) {
        otpCodeRepository.invalidatePreviousOtps(user.getId(), type);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpCode otp = OtpCode.builder()
                .user(user)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();

        otpCodeRepository.save(otp);

        // ── MOCK DELIVERY: print to Spring Boot console ──────────────────
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║  [MOCK {}] OTP for {} ({}) : {} ║", type, user.getName(), user.getPhone(), code);
        log.info("╚══════════════════════════════════════════════╝");

        return code;
    }

    /**
     * Verifies the OTP. Marks it used on success.
     * Throws AppException on mismatch or expiry.
     */
    @Transactional
    public void verify(User user, OtpType type, String providedCode) {
        OtpCode otp = otpCodeRepository
                .findTopByUserIdAndTypeAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
                        user.getId(), type, LocalDateTime.now())
                .orElseThrow(() -> new AppException(
                        "OTP not found or has expired. Please request a new one.", HttpStatus.BAD_REQUEST));

        if (!otp.getCode().equals(providedCode)) {
            throw new AppException("Invalid OTP.", HttpStatus.BAD_REQUEST);
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }
}
