package com.ridemate.service;

import com.ridemate.dto.request.ReportRequest;
import com.ridemate.dto.request.SosRequest;
import com.ridemate.dto.response.BlockedUserResponse;
import com.ridemate.dto.response.ReportResponse;
import com.ridemate.dto.response.SosAlertResponse;
import com.ridemate.entity.*;
import com.ridemate.exception.AppException;
import com.ridemate.repository.*;
import com.ridemate.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyService {

    private final SosAlertRepository    sosAlertRepository;
    private final ReportRepository      reportRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final UserRepository        userRepository;
    private final UserMapper            userMapper;

    // ── SOS ───────────────────────────────────────────────────────────

    @Transactional
    public SosAlertResponse triggerSos(String userEmail, SosRequest req) {
        User user = findUser(userEmail);

        SosAlert alert = SosAlert.builder()
            .user(user)
            .latitude(req.getLatitude())
            .longitude(req.getLongitude())
            .message(req.getMessage())
            .bookingId(req.getBookingId())
            .parcelId(req.getParcelId())
            .build();

        alert = sosAlertRepository.save(alert);

        // Log prominently — no real SMS (as per spec)
        log.error("╔══════════════════════════════════════════════╗");
        log.error("║  🚨  SOS ALERT  🚨                           ║");
        log.error("║  User   : {} (ID: {})                        ║", user.getName(), user.getId());
        log.error("║  Phone  : {}                                  ║", user.getPhone());
        log.error("║  Lat/Lng: {} / {}                             ║", req.getLatitude(), req.getLongitude());
        if (req.getMessage() != null) {
            log.error("║  Message: {}                               ║", req.getMessage());
        }
        if (req.getBookingId() != null) log.error("║  Booking: #{}                              ║", req.getBookingId());
        if (req.getParcelId()  != null) log.error("║  Parcel : #{}                              ║", req.getParcelId());
        log.error("║  Alert ID: {}                                 ║", alert.getId());
        log.error("╚══════════════════════════════════════════════╝");

        return toSosResponse(alert);
    }

    // ── Report ────────────────────────────────────────────────────────

    @Transactional
    public ReportResponse reportUser(String reporterEmail, ReportRequest req) {
        User reporter = findUser(reporterEmail);
        User reported = findUser(req.getReportedUserId());

        if (reporter.getId().equals(reported.getId())) {
            throw new AppException("You cannot report yourself.", HttpStatus.BAD_REQUEST);
        }

        // Prevent duplicate reports for the same booking / parcel
        if (req.getBookingId() != null &&
                reportRepository.existsByReporterIdAndReportedIdAndBookingId(
                    reporter.getId(), reported.getId(), req.getBookingId())) {
            throw new AppException("You have already reported this user for this booking.",
                HttpStatus.CONFLICT);
        }
        if (req.getParcelId() != null &&
                reportRepository.existsByReporterIdAndReportedIdAndParcelId(
                    reporter.getId(), reported.getId(), req.getParcelId())) {
            throw new AppException("You have already reported this user for this parcel.",
                HttpStatus.CONFLICT);
        }

        Report report = Report.builder()
            .reporter(reporter)
            .reported(reported)
            .reason(req.getReason())
            .details(req.getDetails())
            .bookingId(req.getBookingId())
            .parcelId(req.getParcelId())
            .build();

        log.warn("[REPORT] {} reported {} | Reason: {} | Booking: {} | Parcel: {}",
            reporter.getName(), reported.getName(),
            req.getReason(), req.getBookingId(), req.getParcelId());

        return toReportResponse(reportRepository.save(report));
    }

    // ── Block ─────────────────────────────────────────────────────────

    @Transactional
    public BlockedUserResponse blockUser(String blockerEmail, Long blockedUserId) {
        User blocker = findUser(blockerEmail);
        User blocked = findUser(blockedUserId);

        if (blocker.getId().equals(blocked.getId())) {
            throw new AppException("You cannot block yourself.", HttpStatus.BAD_REQUEST);
        }
        if (blockedUserRepository.existsByBlockerIdAndBlockedId(
                blocker.getId(), blocked.getId())) {
            throw new AppException("You have already blocked this user.", HttpStatus.CONFLICT);
        }

        BlockedUser entry = BlockedUser.builder()
            .blocker(blocker)
            .blocked(blocked)
            .build();

        return toBlockedResponse(blockedUserRepository.save(entry));
    }

    @Transactional
    public void unblockUser(String blockerEmail, Long blockedUserId) {
        User blocker = findUser(blockerEmail);
        BlockedUser entry = blockedUserRepository
            .findByBlockerIdAndBlockedId(blocker.getId(), blockedUserId)
            .orElseThrow(() -> new AppException("Block record not found.", HttpStatus.NOT_FOUND));
        blockedUserRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<BlockedUserResponse> getBlockedUsers(String email) {
        User user = findUser(email);
        return blockedUserRepository.findByBlockerIdOrderByCreatedAtDesc(user.getId())
            .stream().map(this::toBlockedResponse).toList();
    }

    // ── Mappers ───────────────────────────────────────────────────────

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

    private BlockedUserResponse toBlockedResponse(BlockedUser b) {
        User blocked = b.getBlocked();
        return BlockedUserResponse.builder()
            .id(b.getId())
            .blockedUserId(blocked.getId())
            .blockedUserName(blocked.getName())
            .blockedUserPhotoUrl(userMapper.toUrl(blocked.getPhotoPath()))
            .createdAt(b.getCreatedAt())
            .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }
}
