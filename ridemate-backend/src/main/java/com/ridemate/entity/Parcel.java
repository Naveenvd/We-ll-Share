package com.ridemate.entity;

import com.ridemate.enums.ParcelSize;
import com.ridemate.enums.ParcelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a parcel delivery request from a sender to be carried on a Ride.
 *
 * Lifecycle:
 *  Sender posts parcel (PENDING, linked to a ride) →
 *  Driver accepts (ACCEPTED, pickup OTP generated) →
 *  Driver verifies pickup OTP + uploads before-photo (IN_TRANSIT) →
 *  Driver uploads after-photo + verifies delivery OTP (DELIVERED)
 *
 * Chat for the parcel uses the Message entity with parcelId set.
 */
@Entity
@Table(name = "parcels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Participants ──────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** The ride this parcel will travel on */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    // ── Locations ─────────────────────────────────────────────────

    @Column(nullable = false)
    private String fromLocation;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal fromLat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal fromLng;

    @Column(nullable = false)
    private String toLocation;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal toLat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal toLng;

    // ── Parcel details ────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParcelSize size;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Sender's photo of the parcel (relative path under ./uploads/) */
    private String photoPath;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Sender confirms no restricted/dangerous items */
    @Column(nullable = false)
    private boolean restrictedItemsAcknowledged;

    // ── Status ────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ParcelStatus status = ParcelStatus.PENDING;

    // ── Pickup flow ───────────────────────────────────────────────

    /** 4-digit OTP generated when driver accepts; shown to sender */
    private String pickupOtp;

    private boolean pickupOtpVerified;

    /** Photo taken by the driver at the pickup point */
    private String beforePhotoPath;

    // ── Delivery flow ─────────────────────────────────────────────

    /** 4-digit OTP generated when parcel goes IN_TRANSIT; shown to sender */
    private String deliveryOtp;

    private boolean deliveryOtpVerified;

    /** Photo taken by the driver at the delivery point */
    private String afterPhotoPath;

    // ── Timestamps ────────────────────────────────────────────────

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
