package com.ridemate.dto.response;

import lombok.*;

@Data @Builder
public class EmergencyContactResponse {
    private Long id;
    private String name;
    private String phone;
    private String relation;
}
