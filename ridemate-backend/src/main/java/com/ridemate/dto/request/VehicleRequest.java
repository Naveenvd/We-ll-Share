package com.ridemate.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VehicleRequest {

    @NotBlank(message = "Vehicle model is required")
    @Size(max = 120)
    private String model;

    @NotBlank(message = "Number plate is required")
    @Pattern(
        regexp = "^[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}$",
        message = "Enter a valid Indian number plate (e.g. MH12AB1234)"
    )
    private String numberPlate;

    @NotBlank(message = "Color is required")
    @Size(max = 50)
    private String color;

    @Min(value = 1, message = "Seats must be at least 1")
    @Max(value = 10, message = "Seats cannot exceed 10")
    private int seats;
}
