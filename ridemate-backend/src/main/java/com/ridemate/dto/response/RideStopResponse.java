package com.ridemate.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder
public class RideStopResponse {
    private Long id;
    private String stopName;
    private BigDecimal lat;
    private BigDecimal lng;
    private int sequence;
}
