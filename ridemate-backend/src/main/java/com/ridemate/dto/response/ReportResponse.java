package com.ridemate.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class ReportResponse {
    private Long          id;
    private Long          reporterId;
    private String        reporterName;
    private Long          reportedId;
    private String        reportedName;
    private String        reason;
    private String        details;
    private Long          bookingId;
    private Long          parcelId;
    private String        resolution;
    private boolean       resolved;
    private LocalDateTime createdAt;
}
