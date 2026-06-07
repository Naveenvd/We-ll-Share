package com.ridemate.dto.response;

import com.ridemate.enums.Gender;
import com.ridemate.enums.Role;
import com.ridemate.enums.UserStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Gender gender;
    private LocalDate dob;
    private Role role;
    private UserStatus status;
    private String rejectionReason;

    // URLs served by FileController
    private String photoUrl;
    private String aadhaarDocUrl;
    private String dlDocUrl;

    private String aadhaarNumber;
    private String dlNumber;

    private boolean phoneVerified;
    private BigDecimal avgRating;
    private int totalRides;
    private int totalParcelsDelivered;
    private LocalDateTime createdAt;
}
