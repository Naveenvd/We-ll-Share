package com.ridemate.controller;

import com.ridemate.dto.request.ChatMessageRequest;
import com.ridemate.dto.response.MessageResponse;
import com.ridemate.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Handles both WebSocket STOMP messages and REST HTTP endpoints for chat.
 *
 * WebSocket endpoint:
 *   Client connects to  ws://localhost:8080/ws  (SockJS)
 *   Sends to            /app/chat.booking.{bookingId}
 *   Receives from       /topic/booking.{bookingId}
 *
 * REST endpoints (for initial history load):
 *   GET  /api/chat/booking/{bookingId}          — fetch history
 *   POST /api/chat/booking/{bookingId}/read     — mark messages read
 */
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ── WebSocket: send a chat message ────────────────────────────────

    /**
     * Spring resolves `principal.getName()` to the JWT-authenticated email
     * because JwtAuthFilter sets the SecurityContext, which STOMP inherits.
     */
    @MessageMapping("/chat.booking.{bookingId}")
    public void sendMessage(
            @DestinationVariable Long bookingId,
            @Payload @Valid ChatMessageRequest req,
            Principal principal) {

        chatService.sendMessage(principal.getName(), bookingId, req);
    }

    // ── REST: load history ────────────────────────────────────────────

    @GetMapping("/api/chat/booking/{bookingId}")
    public ResponseEntity<List<MessageResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
            chatService.getHistory(userDetails.getUsername(), bookingId));
    }

    // ── REST: mark messages read ──────────────────────────────────────

    @PostMapping("/api/chat/booking/{bookingId}/read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookingId) {

        chatService.markRead(userDetails.getUsername(), bookingId);
        return ResponseEntity.noContent().build();
    }

    // ── WebSocket: parcel chat ────────────────────────────────────────

    @MessageMapping("/chat.parcel.{parcelId}")
    public void sendParcelMessage(
            @DestinationVariable Long parcelId,
            @Payload @Valid ChatMessageRequest req,
            Principal principal) {

        chatService.sendParcelMessage(principal.getName(), parcelId, req);
    }

    // ── REST: parcel chat history ─────────────────────────────────────

    @GetMapping("/api/chat/parcel/{parcelId}")
    public ResponseEntity<List<MessageResponse>> getParcelHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long parcelId) {

        return ResponseEntity.ok(
            chatService.getParcelHistory(userDetails.getUsername(), parcelId));
    }

    @PostMapping("/api/chat/parcel/{parcelId}/read")
    public ResponseEntity<Void> markParcelRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long parcelId) {

        chatService.markParcelRead(userDetails.getUsername(), parcelId);
        return ResponseEntity.noContent().build();
    }
}
