package com.ridemate.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DlUploadRequest {

    @NotBlank(message = "Driving licence number is required")
    @Size(min = 10, max = 30, message = "Enter a valid DL number")
    private String dlNumber;
}
