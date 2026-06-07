package com.ridemate.controller;

import com.ridemate.dto.request.ReviewRequest;
import com.ridemate.dto.response.ReviewResponse;
import com.ridemate.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for user reviews.
 *
 * POST  /api/reviews              — Submit a review (tied to booking or parcel)
 * GET   /api/reviews/user/{id}    — Get all reviews received by user {id}
 * GET   /api/reviews/my           — Get all reviews the current user has written
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** Submit a 1–5 star review for a counter-party. */
    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.submitReview(userDetails.getUsername(), req));
    }

    /** Get all reviews received by a specific user (public-ish profile). */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(reviewService.getReviewsForUser(userId));
    }

    /** Get all reviews the current user has written. */
    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(reviewService.getMyReviews(userDetails.getUsername()));
    }
}
