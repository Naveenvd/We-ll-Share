package com.ridemate.dto.response;

import com.ridemate.enums.Role;
import com.ridemate.enums.UserStatus;
import lombok.*;

@Data @Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private boolean phoneVerified;
}
