package com.ridemate.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class VehicleResponse {
    private Long id;
    private String model;
    private String numberPlate;
    private String color;
    private int seats;
    private LocalDateTime createdAt;
}
