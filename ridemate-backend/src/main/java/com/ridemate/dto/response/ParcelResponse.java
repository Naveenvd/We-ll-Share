package com.ridemate.dto.response;

import com.ridemate.enums.ParcelSize;
import com.ridemate.enums.ParcelStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class ParcelResponse {

    private Long id;

    /** Who sent the parcel */
    private PassengerSummaryResponse sender;

    /** The ride this parcel is being carried on */
    private RideResponse ride;

    // ── Locations ─────────────────────────────────────────────────
    private String fromLocation;
    private BigDecimal fromLat;
    private BigDecimal fromLng;
    private String toLocation;
    private BigDecimal toLat;
    private BigDecimal toLng;

    // ── Parcel details ────────────────────────────────────────────
    private ParcelSize size;
    private String description;
    private String photoUrl;
    private BigDecimal price;
    private boolean restrictedItemsAcknowledged;

    // ── Status ────────────────────────────────────────────────────
    private ParcelStatus status;

    // ── Pickup flow ───────────────────────────────────────────────
    /**
     * Pickup OTP — shown ONLY to the sender (once ACCEPTED).
     * Null if the viewer is the driver or status is PENDING.
     */
    private String pickupOtp;
    private boolean pickupOtpVerified;
    private String beforePhotoUrl;

    // ── Delivery flow ─────────────────────────────────────────────
    /** Delivery OTP — shown ONLY to the sender (once IN_TRANSIT) */
    private String deliveryOtp;
    private boolean deliveryOtpVerified;
    private String afterPhotoUrl;

    // ── Meta ──────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private long unreadMessages;
}
