package com.ridemate.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AadhaarUploadRequest {

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^\\d{12}$", message = "Aadhaar number must be exactly 12 digits")
    private String aadhaarNumber;
}
