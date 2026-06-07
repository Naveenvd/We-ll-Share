package com.ridemate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TripOtpRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "Trip OTP must be exactly 4 digits")
    private String otp;
}
