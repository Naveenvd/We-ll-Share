package com.ridemate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    @NotNull(message = "Reported user ID is required")
    private Long reportedUserId;

    /**
     * One of: DANGEROUS_DRIVING, INAPPROPRIATE_BEHAVIOR,
     *         FRAUD, HARASSMENT, NO_SHOW, OTHER
     */
    @NotBlank(message = "Reason category is required")
    private String reason;

    private String details;    // optional free-text elaboration
    private Long   bookingId;  // context
    private Long   parcelId;
}
