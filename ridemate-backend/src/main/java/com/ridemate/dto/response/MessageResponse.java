package com.ridemate.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MessageResponse {

    private Long id;
    private Long bookingId;
    private Long parcelId;
    private Long senderId;
    private String senderName;
    private String senderPhotoUrl;
    private String text;
    private LocalDateTime sentAt;
    private boolean read;
}
