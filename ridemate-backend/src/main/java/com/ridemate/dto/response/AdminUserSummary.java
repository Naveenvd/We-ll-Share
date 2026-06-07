package com.ridemate.dto.response;

import com.ridemate.enums.Gender;
import com.ridemate.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

/** Lightweight user row used in admin lists and verification queue */
@Data @Builder
public class AdminUserSummary {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Gender gender;
    private UserStatus status;
    private String rejectionReason;
    private boolean phoneVerified;
    private String aadhaarNumber;
    private String aadhaarDocUrl;
    private String dlNumber;
    private String dlDocUrl;
    private String photoUrl;
    private LocalDateTime createdAt;
}
