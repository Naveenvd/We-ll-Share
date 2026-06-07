package com.ridemate.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "Reviewed user ID is required")
    private Long reviewedUserId;

    /** 1 to 5 inclusive */
    @NotNull(message = "Rating is required")
    @Min(value = 1,  message = "Rating must be at least 1")
    @Max(value = 5,  message = "Rating must be at most 5")
    private Integer rating;

    /** Optional written comment */
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;

    /** Exactly one of these must be non-null (validated at service layer) */
    private Long bookingId;
    private Long parcelId;
}
