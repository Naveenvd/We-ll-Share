package com.ridemate.controller;

import com.ridemate.dto.request.ResolveReportRequest;
import com.ridemate.dto.request.VerifyUserRequest;
import com.ridemate.dto.response.*;
import com.ridemate.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")   // Belt-and-suspenders: SecurityConfig already restricts /api/admin/**
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ── Dashboard ──────────────────────────────────────────────────

    /** GET /api/admin/stats */
    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStats> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ── Verification queue ─────────────────────────────────────────

    /**
     * GET /api/admin/verifications?page=0&size=20
     * Returns paginated list of PENDING_VERIFICATION users.
     */
    @GetMapping("/verifications")
    public ResponseEntity<Page<AdminUserSummary>> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getPendingVerifications(page, size));
    }

    /**
     * POST /api/admin/verifications/{userId}
     * Body: { "approve": true } or { "approve": false, "rejectionReason": "..." }
     */
    @PostMapping("/verifications/{userId}")
    public ResponseEntity<AdminUserSummary> verifyUser(
            @PathVariable Long userId,
            @Valid @RequestBody VerifyUserRequest req) {
        return ResponseEntity.ok(adminService.verifyUser(userId, req));
    }

    // ── User management ────────────────────────────────────────────

    /**
     * GET /api/admin/users?query=john&page=0&size=20
     */
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserSummary>> listUsers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listUsers(query, page, size));
    }

    /** GET /api/admin/users/{userId} */
    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserSummary> getUserDetail(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserDetail(userId));
    }

    /** POST /api/admin/users/{userId}/suspend */
    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<AdminUserSummary> suspendUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.suspendUser(userId));
    }

    /** POST /api/admin/users/{userId}/unblock */
    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<AdminUserSummary> unblockUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.unblockUser(userId));
    }

    // ── SOS Alerts ─────────────────────────────────────────────────

    /**
     * GET /api/admin/sos
     * Returns all unacknowledged SOS alerts, newest first.
     */
    @GetMapping("/sos")
    public ResponseEntity<List<SosAlertResponse>> getOpenSosAlerts() {
        return ResponseEntity.ok(adminService.getOpenSosAlerts());
    }

    /**
     * POST /api/admin/sos/{alertId}/acknowledge
     * Marks the SOS alert as handled.
     */
    @PostMapping("/sos/{alertId}/acknowledge")
    public ResponseEntity<SosAlertResponse> acknowledgeSosAlert(
            @PathVariable Long alertId) {
        return ResponseEntity.ok(adminService.acknowledgeSosAlert(alertId));
    }

    // ── Reports ───────────────────────────────────────────────────

    /**
     * GET /api/admin/reports
     * Returns all unresolved user reports, newest first.
     */
    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponse>> getOpenReports() {
        return ResponseEntity.ok(adminService.getOpenReports());
    }

    /**
     * POST /api/admin/reports/{reportId}/resolve
     * Body: { "resolution": "..." }
     * Marks the report resolved with an admin-written note.
     */
    @PostMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ResolveReportRequest req) {
        return ResponseEntity.ok(adminService.resolveReport(reportId, req));
    }
}
