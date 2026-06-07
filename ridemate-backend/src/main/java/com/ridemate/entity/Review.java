package com.ridemate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A user-to-user review left after a completed booking or delivered parcel.
 *
 * Rules enforced at service layer:
 *  - Rating must be 1–5.
 *  - A reviewer can rate the same counter-party at most once per booking (or parcel).
 *  - You cannot review yourself.
 *  - Reviews are only allowed after terminal status (COMPLETED / DELIVERED).
 */
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = {
        // One review per reviewer per booking
        @UniqueConstraint(name = "uk_review_booking",
            columnNames = {"reviewer_id", "reviewed_id", "booking_id"}),
        // One review per reviewer per parcel
        @UniqueConstraint(name = "uk_review_parcel",
            columnNames = {"reviewer_id", "reviewed_id", "parcel_id"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Who is writing the review */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    /** Who is being reviewed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_id", nullable = false)
    private User reviewed;

    /** 1–5 star rating */
    @Column(nullable = false)
    private int rating;

    /** Optional written comment */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /** Context — one of the two must be set */
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "parcel_id")
    private Long parcelId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
