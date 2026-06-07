package com.ridemate.repository;

import com.ridemate.entity.OtpCode;
import com.ridemate.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    /** Latest unused, unexpired OTP for a user+type combination */
    Optional<OtpCode> findTopByUserIdAndTypeAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
            Long userId, OtpType type, LocalDateTime now);

    /** Mark all previous OTPs of this type+user as used (invalidate before issuing new one) */
    @Modifying
    @Query("UPDATE OtpCode o SET o.used = true WHERE o.user.id = :userId AND o.type = :type AND o.used = false")
    void invalidatePreviousOtps(Long userId, OtpType type);
}
