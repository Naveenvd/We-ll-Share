package com.ridemate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Body for POST /api/admin/reports/{id}/resolve */
@Data
public class ResolveReportRequest {

    @NotBlank(message = "Resolution text is required")
    private String resolution;
}
