package com.ridemate.entity;

import com.ridemate.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @Column(name = "seats_booked", nullable = false)
    @Builder.Default
    private int seatsBooked = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    /** 4-digit OTP generated when driver starts the ride; shown to passenger */
    @Column(name = "trip_otp", length = 10)
    private String tripOtp;

    @Column(name = "otp_verified", nullable = false)
    @Builder.Default
    private boolean otpVerified = false;

    /** Unique token for the public trip-share tracking URL */
    @Column(name = "trip_share_token", unique = true, length = 64)
    private String tripShareToken;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
