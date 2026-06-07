package com.ridemate.dto.response;

import com.ridemate.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class BookingResponse {

    private Long id;
    private RideResponse ride;
    private PassengerSummaryResponse passenger;

    private int seatsBooked;
    private BookingStatus status;

    /**
     * Trip OTP — only populated for the passenger AFTER the driver starts the ride.
     * Null in all other scenarios to prevent leaking.
     */
    private String tripOtp;
    private boolean otpVerified;

    /** Public URL for real-time tracking — shared after booking is APPROVED */
    private String tripShareToken;

    private BigDecimal amount;
    private LocalDateTime createdAt;

    /** Unread message count for this booking's chat */
    private long unreadMessages;
}
