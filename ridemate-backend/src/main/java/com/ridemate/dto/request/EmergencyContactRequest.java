package com.ridemate.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EmergencyContactRequest {

    @NotBlank(message = "Contact name is required")
    @Size(max = 120)
    private String name;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phone;

    @NotBlank(message = "Relation is required")
    @Size(max = 60)
    private String relation;
}
