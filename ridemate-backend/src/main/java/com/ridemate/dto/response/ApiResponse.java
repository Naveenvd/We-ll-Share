package com.ridemate.dto.response;

import lombok.*;

/** Generic success/message wrapper for simple responses. */
@Data @AllArgsConstructor @NoArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;

    public static ApiResponse ok(String message) {
        return new ApiResponse(true, message);
    }
}
