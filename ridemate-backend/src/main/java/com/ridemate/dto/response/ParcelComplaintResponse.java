package com.ridemate.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class ParcelComplaintResponse {
    private Long   id;
    private Long   parcelId;
    private Long   raisedById;
    private String raisedByName;
    private String reason;
    private String resolution;
    private LocalDateTime createdAt;
}
