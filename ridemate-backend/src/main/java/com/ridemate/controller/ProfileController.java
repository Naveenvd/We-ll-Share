package com.ridemate.controller;

import com.ridemate.dto.request.*;
import com.ridemate.dto.response.*;
import com.ridemate.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // ── Profile CRUD ───────────────────────────────────────────────

    /** GET /api/profile — view own profile */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(profileService.getProfile(principal.getUsername()));
    }

    /** PUT /api/profile — update name / gender / dob */
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(profileService.updateProfile(principal.getUsername(), req));
    }

    /** POST /api/profile/photo — replace profile photo (multipart) */
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> uploadPhoto(
            @AuthenticationPrincipal UserDetails principal,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadPhoto(principal.getUsername(), file));
    }

    // ── Document uploads ───────────────────────────────────────────

    /**
     * POST /api/profile/aadhaar
     * Form fields: aadhaarNumber (text) + file (multipart)
     */
    @PostMapping(value = "/aadhaar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> uploadAadhaar(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @ModelAttribute AadhaarUploadRequest meta,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadAadhaar(principal.getUsername(), meta, file));
    }

    /**
     * POST /api/profile/dl
     * Form fields: dlNumber (text) + file (multipart)
     */
    @PostMapping(value = "/dl", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> uploadDl(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @ModelAttribute DlUploadRequest meta,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadDl(principal.getUsername(), meta, file));
    }

    // ── Vehicles ───────────────────────────────────────────────────

    /** GET /api/profile/vehicles */
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleResponse>> getVehicles(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(profileService.getVehicles(principal.getUsername()));
    }

    /** POST /api/profile/vehicles */
    @PostMapping("/vehicles")
    public ResponseEntity<VehicleResponse> addVehicle(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody VehicleRequest req) {
        return ResponseEntity.ok(profileService.addVehicle(principal.getUsername(), req));
    }

    /** PUT /api/profile/vehicles/{id} */
    @PutMapping("/vehicles/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest req) {
        return ResponseEntity.ok(profileService.updateVehicle(principal.getUsername(), id, req));
    }

    /** DELETE /api/profile/vehicles/{id} */
    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<ApiResponse> deleteVehicle(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(profileService.deleteVehicle(principal.getUsername(), id));
    }

    // ── Emergency Contacts ─────────────────────────────────────────

    /** GET /api/profile/emergency-contacts */
    @GetMapping("/emergency-contacts")
    public ResponseEntity<List<EmergencyContactResponse>> getEmergencyContacts(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(profileService.getEmergencyContacts(principal.getUsername()));
    }

    /** POST /api/profile/emergency-contacts */
    @PostMapping("/emergency-contacts")
    public ResponseEntity<EmergencyContactResponse> addEmergencyContact(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody EmergencyContactRequest req) {
        return ResponseEntity.ok(profileService.addEmergencyContact(principal.getUsername(), req));
    }

    /** PUT /api/profile/emergency-contacts/{id} */
    @PutMapping("/emergency-contacts/{id}")
    public ResponseEntity<EmergencyContactResponse> updateEmergencyContact(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody EmergencyContactRequest req) {
        return ResponseEntity.ok(
                profileService.updateEmergencyContact(principal.getUsername(), id, req));
    }

    /** DELETE /api/profile/emergency-contacts/{id} */
    @DeleteMapping("/emergency-contacts/{id}")
    public ResponseEntity<ApiResponse> deleteEmergencyContact(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                profileService.deleteEmergencyContact(principal.getUsername(), id));
    }
}
