package com.ridemate.controller;

import com.ridemate.dto.request.ReportRequest;
import com.ridemate.dto.request.SosRequest;
import com.ridemate.dto.response.BlockedUserResponse;
import com.ridemate.dto.response.ReportResponse;
import com.ridemate.dto.response.SosAlertResponse;
import com.ridemate.service.SafetyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for safety features:
 *  - SOS alert trigger
 *  - User reporting
 *  - Block / unblock / list blocked users
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SafetyController {

    private final SafetyService safetyService;

    // ── SOS ───────────────────────────────────────────────────────────────

    /**
     * POST /api/sos
     * Trigger an SOS alert. Saves location + context and logs prominently.
     * No real SMS is sent (as per spec).
     */
    @PostMapping("/sos")
    public ResponseEntity<SosAlertResponse> triggerSos(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SosRequest req) {

        SosAlertResponse response =
                safetyService.triggerSos(userDetails.getUsername(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Reports ───────────────────────────────────────────────────────────

    /**
     * POST /api/reports
     * Report another user for bad behaviour. Optionally tied to a booking or parcel.
     */
    @PostMapping("/reports")
    public ResponseEntity<ReportResponse> reportUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReportRequest req) {

        ReportResponse response =
                safetyService.reportUser(userDetails.getUsername(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Block / Unblock ───────────────────────────────────────────────────

    /**
     * POST /api/blocked-users/{userId}
     * Block the specified user. Idempotency: 409 if already blocked.
     */
    @PostMapping("/blocked-users/{userId}")
    public ResponseEntity<BlockedUserResponse> blockUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId) {

        BlockedUserResponse response =
                safetyService.blockUser(userDetails.getUsername(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /api/blocked-users/{userId}
     * Unblock the specified user.
     */
    @DeleteMapping("/blocked-users/{userId}")
    public ResponseEntity<Void> unblockUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId) {

        safetyService.unblockUser(userDetails.getUsername(), userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/blocked-users
     * List all users blocked by the current user, newest first.
     */
    @GetMapping("/blocked-users")
    public ResponseEntity<List<BlockedUserResponse>> getBlockedUsers(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                safetyService.getBlockedUsers(userDetails.getUsername()));
    }
}
