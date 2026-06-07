package com.ridemate.dto.response;

import com.ridemate.enums.Gender;
import lombok.*;
import java.math.BigDecimal;

@Data @Builder
public class PassengerSummaryResponse {
    private Long id;
    private String name;
    private String photoUrl;
    private Gender gender;
    private BigDecimal avgRating;
    private int totalRides;
}
