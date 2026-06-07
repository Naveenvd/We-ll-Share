package com.ridemate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A complaint raised by the sender against a parcel delivery.
 * Used for dispute resolution (admin reviews in Phase 7).
 */
@Entity
@Table(name = "parcel_complaints")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParcelComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcel_id", nullable = false)
    private Parcel parcel;

    /** The user who raised the complaint (sender or driver) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by_id", nullable = false)
    private User raisedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    /** Admin resolution note — set in Phase 7 */
    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
