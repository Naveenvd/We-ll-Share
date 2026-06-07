package com.ridemate.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @Min(value = 1, message = "Must book at least 1 seat")
    @Max(value = 10, message = "Cannot book more than 10 seats at once")
    private int seatsBooked = 1;
}
