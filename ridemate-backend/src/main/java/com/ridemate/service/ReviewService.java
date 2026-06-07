package com.ridemate.service;

import com.ridemate.dto.request.ReviewRequest;
import com.ridemate.dto.response.ReviewResponse;
import com.ridemate.entity.*;
import com.ridemate.enums.BookingStatus;
import com.ridemate.enums.ParcelStatus;
import com.ridemate.exception.AppException;
import com.ridemate.repository.*;
import com.ridemate.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Handles the full review lifecycle:
 *  - Submit a 1–5 star review (tied to a booking or parcel)
 *  - Guard against self-review, duplicate reviews, and reviews on non-terminal records
 *  - Recalculates and persists the reviewed user's avgRating after each submission
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository   reviewRepository;
    private final UserRepository     userRepository;
    private final BookingRepository  bookingRepository;
    private final ParcelRepository   parcelRepository;
    private final UserMapper         userMapper;

    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public ReviewResponse submitReview(String reviewerEmail, ReviewRequest req) {

        User reviewer = findUser(reviewerEmail);
        User reviewed = findUser(req.getReviewedUserId());

        // Self-review guard
        if (reviewer.getId().equals(reviewed.getId())) {
            throw new AppException("You cannot review yourself.", HttpStatus.BAD_REQUEST);
        }

        // Exactly one context must be provided
        if (req.getBookingId() == null && req.getParcelId() == null) {
            throw new AppException(
                    "A bookingId or parcelId is required to submit a review.",
                    HttpStatus.BAD_REQUEST);
        }

        // ── Booking-context review ────────────────────────────────────────
        if (req.getBookingId() != null) {
            Booking booking = bookingRepository.findById(req.getBookingId())
                    .orElseThrow(() -> new AppException("Booking not found.", HttpStatus.NOT_FOUND));

            if (booking.getStatus() != BookingStatus.COMPLETED) {
                throw new AppException(
                        "Reviews are only allowed after the booking is COMPLETED.",
                        HttpStatus.BAD_REQUEST);
            }

            if (reviewRepository.existsByReviewerIdAndReviewedIdAndBookingId(
                    reviewer.getId(), reviewed.getId(), req.getBookingId())) {
                throw new AppException(
                        "You have already reviewed this user for this booking.",
                        HttpStatus.CONFLICT);
            }
        }

        // ── Parcel-context review ─────────────────────────────────────────
        if (req.getParcelId() != null) {
            Parcel parcel = parcelRepository.findById(req.getParcelId())
                    .orElseThrow(() -> new AppException("Parcel not found.", HttpStatus.NOT_FOUND));

            if (parcel.getStatus() != ParcelStatus.DELIVERED) {
                throw new AppException(
                        "Reviews are only allowed after the parcel is DELIVERED.",
                        HttpStatus.BAD_REQUEST);
            }

            if (reviewRepository.existsByReviewerIdAndReviewedIdAndParcelId(
                    reviewer.getId(), reviewed.getId(), req.getParcelId())) {
                throw new AppException(
                        "You have already reviewed this user for this parcel.",
                        HttpStatus.CONFLICT);
            }
        }

        // Persist review
        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewed(reviewed)
                .rating(req.getRating())
                .comment(req.getComment())
                .bookingId(req.getBookingId())
                .parcelId(req.getParcelId())
                .build();

        review = reviewRepository.save(review);

        // Update reviewed user's average rating
        recalculateAvgRating(reviewed);

        return toResponse(review);
    }

    // ── Query ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForUser(Long userId) {
        return reviewRepository.findByReviewedIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(String email) {
        User user = findUser(email);
        return reviewRepository.findByReviewerIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Recalculates the average rating from all persisted reviews and
     * saves it back to the User row.
     */
    private void recalculateAvgRating(User user) {
        Double avg = reviewRepository.calculateAvgRating(user.getId());
        user.setAvgRating(avg != null
                ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        userRepository.save(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .reviewerId(r.getReviewer().getId())
                .reviewerName(r.getReviewer().getName())
                .reviewerPhotoUrl(userMapper.toUrl(r.getReviewer().getPhotoPath()))
                .reviewedId(r.getReviewed().getId())
                .reviewedName(r.getReviewed().getName())
                .rating(r.getRating())
                .comment(r.getComment())
                .bookingId(r.getBookingId())
                .parcelId(r.getParcelId())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
