package com.ridemate.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SosRequest {
    private BigDecimal latitude;   // nullable — user may deny location
    private BigDecimal longitude;
    private String     message;    // optional short message
    private Long       bookingId;  // optional context
    private Long       parcelId;
}
