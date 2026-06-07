package com.ridemate.service;

import com.ridemate.dto.request.ResolveReportRequest;
import com.ridemate.dto.request.VerifyUserRequest;
import com.ridemate.dto.response.*;
import com.ridemate.entity.Report;
import com.ridemate.entity.SosAlert;
import com.ridemate.entity.User;
import com.ridemate.enums.UserStatus;
import com.ridemate.exception.AppException;
import com.ridemate.repository.*;
import com.ridemate.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminUserRepository        adminUserRepository;
    private final RideRepository             rideRepository;
    private final ParcelRepository           parcelRepository;
    private final ParcelComplaintRepository  complaintRepository;
    private final SosAlertRepository         sosAlertRepository;
    private final ReportRepository           reportRepository;
    private final UserMapper                 mapper;

    // ── Dashboard stats ────────────────────────────────────────────

    public AdminDashboardStats getDashboardStats() {
        long total        = adminUserRepository.count();
        long pending      = adminUserRepository.countByStatus(UserStatus.PENDING_VERIFICATION);
        long verified     = adminUserRepository.countByStatus(UserStatus.VERIFIED);
        long totalRides   = rideRepository.count();
        long totalParcels = parcelRepository.count();
        long openSos      = sosAlertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc().size();
        long openReports  = reportRepository.findByResolvedFalseOrderByCreatedAtDesc().size();

        // B13 fix: count parcel complaints with no resolution note as open disputes
        long openDisputes = complaintRepository.countByResolutionIsNull();

        return AdminDashboardStats.builder()
                .totalUsers(total)
                .pendingVerifications(pending)
                .verifiedUsers(verified)
                .totalRides(totalRides)
                .totalParcels(totalParcels)
                .openSosAlerts(openSos)
                .openDisputes(openDisputes)
                .openReports(openReports)
                .build();
    }

    // ── Verification queue ─────────────────────────────────────────

    /** Returns all users in PENDING_VERIFICATION, oldest first, paginated. */
    public Page<AdminUserSummary> getPendingVerifications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return adminUserRepository
                .findByStatusOrderByCreatedAtAsc(UserStatus.PENDING_VERIFICATION, pageable)
                .map(mapper::toAdminSummary);
    }

    /** Approve or reject a user. */
    @Transactional
    public AdminUserSummary verifyUser(Long userId, VerifyUserRequest req) {
        User user = findById(userId);

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new AppException(
                    "User is not in PENDING_VERIFICATION status.", HttpStatus.BAD_REQUEST);
        }

        if (Boolean.TRUE.equals(req.getApprove())) {
            user.setStatus(UserStatus.VERIFIED);
            user.setRejectionReason(null);
        } else {
            if (req.getRejectionReason() == null || req.getRejectionReason().isBlank()) {
                throw new AppException("Rejection reason is required.", HttpStatus.BAD_REQUEST);
            }
            user.setStatus(UserStatus.REJECTED);
            user.setRejectionReason(req.getRejectionReason());
        }

        return mapper.toAdminSummary(adminUserRepository.save(user));
    }

    // ── User management ────────────────────────────────────────────

    /** Paginated list of all users; supports optional search query. */
    public Page<AdminUserSummary> listUsers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = (query != null && !query.isBlank())
                ? adminUserRepository.searchUsers(query, pageable)
                : adminUserRepository.findAll(pageable);
        return users.map(mapper::toAdminSummary);
    }

    /** Get a single user's full details. */
    public AdminUserSummary getUserDetail(Long userId) {
        return mapper.toAdminSummary(findById(userId));
    }

    /** Suspend a user account. */
    @Transactional
    public AdminUserSummary suspendUser(Long userId) {
        User user = findById(userId);
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AppException("User is already suspended.", HttpStatus.BAD_REQUEST);
        }
        user.setStatus(UserStatus.SUSPENDED);
        return mapper.toAdminSummary(adminUserRepository.save(user));
    }

    /** Unblock / reinstate a suspended user back to PENDING_VERIFICATION for re-review. */
    @Transactional
    public AdminUserSummary unblockUser(Long userId) {
        User user = findById(userId);
        if (user.getStatus() != UserStatus.SUSPENDED) {
            throw new AppException("User is not suspended.", HttpStatus.BAD_REQUEST);
        }
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        return mapper.toAdminSummary(adminUserRepository.save(user));
    }

    // ── SOS Alert management ────────────────────────────────────────

    /**
     * Returns all unacknowledged SOS alerts, newest first.
     * Each alert is mapped to SosAlertResponse for the admin UI.
     */
    @Transactional(readOnly = true)
    public List<SosAlertResponse> getOpenSosAlerts() {
        return sosAlertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc()
                .stream().map(this::toSosResponse).toList();
    }

    /**
     * Marks an SOS alert as acknowledged (handled by admin).
     */
    @Transactional
    public SosAlertResponse acknowledgeSosAlert(Long alertId) {
        SosAlert alert = sosAlertRepository.findById(alertId)
                .orElseThrow(() -> new AppException("SOS alert not found.", HttpStatus.NOT_FOUND));
        alert.setAcknowledged(true);
        return toSosResponse(sosAlertRepository.save(alert));
    }

    // ── Report management ────────────────────────────────────────────

    /**
     * Returns all unresolved reports, newest first.
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getOpenReports() {
        return reportRepository.findByResolvedFalseOrderByCreatedAtDesc()
                .stream().map(this::toReportResponse).toList();
    }

    /**
     * Marks a report as resolved with an admin-written resolution note.
     */
    @Transactional
    public ReportResponse resolveReport(Long reportId, ResolveReportRequest req) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException("Report not found.", HttpStatus.NOT_FOUND));
        if (report.isResolved()) {
            throw new AppException("Report is already resolved.", HttpStatus.BAD_REQUEST);
        }
        report.setResolved(true);
        report.setResolution(req.getResolution());
        return toReportResponse(reportRepository.save(report));
    }

    // ── Private helpers ────────────────────────────────────────────

    private User findById(Long userId) {
        return adminUserRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }

    private SosAlertResponse toSosResponse(SosAlert a) {
        return SosAlertResponse.builder()
                .id(a.getId())
                .userId(a.getUser().getId())
                .userName(a.getUser().getName())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .message(a.getMessage())
                .bookingId(a.getBookingId())
                .parcelId(a.getParcelId())
                .acknowledged(a.isAcknowledged())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private ReportResponse toReportResponse(Report r) {
        return ReportResponse.builder()
                .id(r.getId())
                .reporterId(r.getReporter().getId())
                .reporterName(r.getReporter().getName())
                .reportedId(r.getReported().getId())
                .reportedName(r.getReported().getName())
                .reason(r.getReason())
                .details(r.getDetails())
                .bookingId(r.getBookingId())
                .parcelId(r.getParcelId())
                .resolution(r.getResolution())
                .resolved(r.isResolved())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
