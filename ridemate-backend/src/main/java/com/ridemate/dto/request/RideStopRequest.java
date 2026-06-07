package com.ridemate.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RideStopRequest {

    @NotBlank(message = "Stop name is required")
    private String stopName;

    @NotNull
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private BigDecimal lat;

    @NotNull
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private BigDecimal lng;

    @Min(1)
    private int sequence;
}
