package com.ridemate.dto.request;

import lombok.Data;

@Data
public class BookingActionRequest {
    /** Optional rejection reason (used when rejecting) */
    private String reason;
}
