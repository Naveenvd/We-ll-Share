package com.ridemate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParcelComplaintRequest {

    @NotBlank(message = "Complaint reason is required")
    @Size(min = 10, max = 1000, message = "Please describe the issue in 10–1000 characters")
    private String reason;
}
