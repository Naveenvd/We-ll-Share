package com.ridemate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Payload sent by client over WebSocket STOMP */
@Data
public class ChatMessageRequest {

    @NotBlank
    @Size(max = 2000, message = "Message too long")
    private String text;

    /** Sender's email — injected from the STOMP principal server-side */
    private String senderEmail;
}
