package com.ridemate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A safety report filed by one user against another.
 * Reviewed by admins in Phase 7.
 */
@Entity
@Table(name = "reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", nullable = false)
    private User reported;

    /**
     * Category: DANGEROUS_DRIVING | INAPPROPRIATE_BEHAVIOR |
     *           FRAUD | HARASSMENT | NO_SHOW | OTHER
     */
    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String details;

    /** Optional context fields */
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "parcel_id")
    private Long parcelId;

    /** Admin resolution note — set in Phase 7 */
    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(nullable = false)
    @Builder.Default
    private boolean resolved = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
