package com.ridemate.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class ReviewResponse {
    private Long          id;
    private Long          reviewerId;
    private String        reviewerName;
    private String        reviewerPhotoUrl;
    private Long          reviewedId;
    private String        reviewedName;
    private int           rating;
    private String        comment;
    private Long          bookingId;
    private Long          parcelId;
    private LocalDateTime createdAt;
}
