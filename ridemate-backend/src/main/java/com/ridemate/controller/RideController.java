package com.ridemate.controller;

import com.ridemate.dto.request.RidePostRequest;
import com.ridemate.dto.response.ApiResponse;
import com.ridemate.dto.response.RideResponse;
import com.ridemate.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    /** POST /api/rides — driver posts a new ride */
    @PostMapping
    public ResponseEntity<RideResponse> postRide(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RidePostRequest req) {
        return ResponseEntity.ok(rideService.postRide(principal.getUsername(), req));
    }

    /** GET /api/rides/my — driver views their own posted rides */
    @GetMapping("/my")
    public ResponseEntity<List<RideResponse>> myRides(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(rideService.getMyPostedRides(principal.getUsername()));
    }

    /** GET /api/rides/{id} — get a single ride's details */
    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRide(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(rideService.getRide(id, principal.getUsername()));
    }

    /** DELETE /api/rides/{id} — driver cancels their ride */
    @DeleteMapping("/{id}")
    public ResponseEntity<RideResponse> cancelRide(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(rideService.cancelRide(id, principal.getUsername()));
    }

    /**
     * GET /api/rides/search
     *
     * Query params:
     *   fromLat, fromLng, toLat, toLng  — required — center coordinates
     *   date                            — required — ISO datetime (e.g. 2025-06-10T00:00:00)
     *   seats                           — default 1
     *   minPrice, maxPrice              — optional
     *   womenOnly                       — optional boolean
     *   page, size                      — pagination
     */
    @GetMapping("/search")
    public ResponseEntity<Page<RideResponse>> search(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam BigDecimal fromLat,
            @RequestParam BigDecimal fromLng,
            @RequestParam BigDecimal toLat,
            @RequestParam BigDecimal toLng,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "1")    int seats,
            @RequestParam(required = false)      BigDecimal minPrice,
            @RequestParam(required = false)      BigDecimal maxPrice,
            @RequestParam(required = false)      Boolean womenOnly,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size) {

        return ResponseEntity.ok(rideService.searchRides(
            principal.getUsername(),
            fromLat, fromLng, toLat, toLng,
            date, seats,
            minPrice, maxPrice,
            womenOnly,
            page, size));
    }
}
