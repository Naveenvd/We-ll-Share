package com.ridemate.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class SosAlertResponse {
    private Long          id;
    private Long          userId;
    private String        userName;
    private BigDecimal    latitude;
    private BigDecimal    longitude;
    private String        message;
    private Long          bookingId;
    private Long          parcelId;
    private boolean       acknowledged;
    private LocalDateTime createdAt;
}
