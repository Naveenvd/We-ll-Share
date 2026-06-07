package com.ridemate.controller;

import com.ridemate.dto.response.HistoryItemResponse;
import com.ridemate.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET /api/history
 * Returns the current user's unified activity history:
 *  - Rides posted as driver  (COMPLETED / CANCELLED)
 *  - Bookings made as passenger (COMPLETED / CANCELLED / REJECTED)
 *  - Parcels sent as sender  (DELIVERED / CANCELLED)
 *
 * Results are merged and sorted newest-first.
 */
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<List<HistoryItemResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                historyService.getHistory(userDetails.getUsername()));
    }
}
