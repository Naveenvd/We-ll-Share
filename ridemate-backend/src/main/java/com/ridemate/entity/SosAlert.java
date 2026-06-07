package com.ridemate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Triggered when a user presses the SOS button during an active trip.
 * No real SMS is sent — the alert is logged to the console and stored
 * in the database for admin review (Phase 7).
 */
@Entity
@Table(name = "sos_alerts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SosAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** GPS coordinates at the moment of alert (may be null if denied by browser) */
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    /** Optional short message from the user */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** Context: which booking triggered the SOS (nullable) */
    @Column(name = "booking_id")
    private Long bookingId;

    /** Context: which parcel triggered the SOS (nullable) */
    @Column(name = "parcel_id")
    private Long parcelId;

    /** Whether an admin has acknowledged this alert */
    @Column(nullable = false)
    @Builder.Default
    private boolean acknowledged = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
