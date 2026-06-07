package com.ridemate.repository;

import com.ridemate.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.OptionalDouble;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** All reviews received by a specific user, newest first */
    List<Review> findByReviewedIdOrderByCreatedAtDesc(Long reviewedId);

    /** All reviews written by a specific user, newest first */
    List<Review> findByReviewerIdOrderByCreatedAtDesc(Long reviewerId);

    /** Guard: reviewer cannot rate the same person twice for the same booking */
    boolean existsByReviewerIdAndReviewedIdAndBookingId(
            Long reviewerId, Long reviewedId, Long bookingId);

    /** Guard: reviewer cannot rate the same person twice for the same parcel */
    boolean existsByReviewerIdAndReviewedIdAndParcelId(
            Long reviewerId, Long reviewedId, Long parcelId);

    /**
     * Recalculate average rating for a user after a new review is saved.
     * Returns null (Optional.empty) if no reviews exist yet.
     */
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.reviewed.id = :userId")
    Double calculateAvgRating(Long userId);
}
