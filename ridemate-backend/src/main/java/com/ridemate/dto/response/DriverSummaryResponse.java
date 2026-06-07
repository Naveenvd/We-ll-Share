package com.ridemate.dto.response;

import com.ridemate.enums.Gender;
import lombok.*;
import java.math.BigDecimal;

/** Lightweight driver info embedded in ride search results */
@Data @Builder
public class DriverSummaryResponse {
    private Long id;
    private String name;
    private String photoUrl;
    private Gender gender;
    private BigDecimal avgRating;
    private int totalRides;
}
