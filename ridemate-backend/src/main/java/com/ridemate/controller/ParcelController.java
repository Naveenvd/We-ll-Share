package com.ridemate.controller;

import com.ridemate.dto.request.ParcelComplaintRequest;
import com.ridemate.dto.request.ParcelPostRequest;
import com.ridemate.dto.response.ParcelComplaintResponse;
import com.ridemate.dto.response.ParcelResponse;
import com.ridemate.service.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;

    // ── Sender: post a parcel ────────────────────────────────────────

    /**
     * Multipart request:
     *   - parcel  (JSON part)  → ParcelPostRequest fields
     *   - photo   (file part)  → optional parcel photo
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParcelResponse> postParcel(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("parcel") @Valid ParcelPostRequest req,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(parcelService.postParcel(userDetails.getUsername(), req, photo));
    }

    // ── Sender: view own parcels ─────────────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<List<ParcelResponse>> getMySentParcels(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(parcelService.getMySentParcels(userDetails.getUsername()));
    }

    // ── Sender: cancel ───────────────────────────────────────────────

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ParcelResponse> cancelParcel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(parcelService.cancelParcel(id, userDetails.getUsername()));
    }

    // ── Driver: view all parcels on their rides ──────────────────────

    @GetMapping("/driver")
    public ResponseEntity<List<ParcelResponse>> getMyCarryingParcels(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(parcelService.getMyCarryingParcels(userDetails.getUsername()));
    }

    @GetMapping("/driver/pending")
    public ResponseEntity<List<ParcelResponse>> getPendingParcelsForDriver(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(parcelService.getPendingParcelsForDriver(userDetails.getUsername()));
    }

    /** Driver sees all parcel requests attached to one of their rides */
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<ParcelResponse>> getParcelsForRide(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long rideId) {

        return ResponseEntity.ok(
            parcelService.getParcelsForRide(rideId, userDetails.getUsername()));
    }

    // ── Driver: accept / reject ──────────────────────────────────────

    @PostMapping("/{id}/accept")
    public ResponseEntity<ParcelResponse> acceptParcel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(parcelService.acceptParcel(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ParcelResponse> rejectParcel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(parcelService.rejectParcel(id, userDetails.getUsername()));
    }

    // ── Driver: verify pickup OTP + before-photo ─────────────────────

    @PostMapping(value = "/{id}/pickup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParcelResponse> verifyPickup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam("otp") String otp,
            @RequestPart(value = "beforePhoto", required = false) MultipartFile beforePhoto) {

        return ResponseEntity.ok(
            parcelService.verifyPickupOtp(id, userDetails.getUsername(), otp, beforePhoto));
    }

    // ── Driver: verify delivery OTP + after-photo ────────────────────

    @PostMapping(value = "/{id}/deliver", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParcelResponse> verifyDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam("otp") String otp,
            @RequestPart(value = "afterPhoto", required = false) MultipartFile afterPhoto) {

        return ResponseEntity.ok(
            parcelService.verifyDeliveryOtp(id, userDetails.getUsername(), otp, afterPhoto));
    }

    // ── Any party: raise complaint ───────────────────────────────────

    @PostMapping("/{id}/complaint")
    public ResponseEntity<ParcelComplaintResponse> raiseComplaint(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ParcelComplaintRequest req) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(parcelService.raiseComplaint(id, userDetails.getUsername(), req));
    }

    // ── Any party: single parcel ─────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> getParcel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(parcelService.getParcel(id, userDetails.getUsername()));
    }
}
