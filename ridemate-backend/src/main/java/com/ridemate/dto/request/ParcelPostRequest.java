package com.ridemate.dto.request;

import com.ridemate.enums.ParcelSize;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParcelPostRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotBlank(message = "From location is required")
    private String fromLocation;

    @NotNull private BigDecimal fromLat;
    @NotNull private BigDecimal fromLng;

    @NotBlank(message = "To location is required")
    private String toLocation;

    @NotNull private BigDecimal toLat;
    @NotNull private BigDecimal toLng;

    @NotNull(message = "Parcel size is required")
    private ParcelSize size;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 500, message = "Description must be 5–500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    /** Sender must acknowledge that the parcel contains no restricted items */
    @AssertTrue(message = "You must acknowledge that the parcel contains no restricted items")
    private boolean restrictedItemsAcknowledged;
}
