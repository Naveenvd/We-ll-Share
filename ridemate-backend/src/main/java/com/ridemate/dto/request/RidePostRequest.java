package com.ridemate.dto.request;

import com.ridemate.enums.ParcelSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RidePostRequest {

    @NotBlank(message = "From location name is required")
    private String fromLocation;

    @NotBlank(message = "To location name is required")
    private String toLocation;

    @NotNull @DecimalMin("-90.0")  @DecimalMax("90.0")
    private BigDecimal fromLat;

    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private BigDecimal fromLng;

    @NotNull @DecimalMin("-90.0")  @DecimalMax("90.0")
    private BigDecimal toLat;

    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private BigDecimal toLng;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @Min(value = 1, message = "At least 1 seat must be offered")
    @Max(value = 10, message = "Cannot offer more than 10 seats")
    private int seatsTotal;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal pricePerSeat;

    private boolean acceptsPassengers = true;
    private boolean acceptsParcels    = false;
    private ParcelSize maxParcelSize;

    /** Only female drivers can set this to true */
    private boolean womenOnly = false;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @Valid
    @Size(max = 5, message = "Cannot add more than 5 intermediate stops")
    private List<RideStopRequest> stops = new ArrayList<>();
}
