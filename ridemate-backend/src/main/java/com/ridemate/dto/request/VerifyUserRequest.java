package com.ridemate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyUserRequest {

    /**
     * true  → APPROVE  (status becomes VERIFIED)
     * false → REJECT   (status becomes REJECTED)
     */
    @NotNull
    private Boolean approve;

    /** Required when approve = false */
    private String rejectionReason;
}
