package com.ridemate.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class BlockedUserResponse {
    private Long          id;
    private Long          blockedUserId;
    private String        blockedUserName;
    private String        blockedUserPhotoUrl;
    private LocalDateTime createdAt;
}
