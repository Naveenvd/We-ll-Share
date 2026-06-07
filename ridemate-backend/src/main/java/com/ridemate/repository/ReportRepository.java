package com.ridemate.repository;

import com.ridemate.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    List<Report> findByResolvedFalseOrderByCreatedAtDesc();

    boolean existsByReporterIdAndReportedIdAndBookingId(
        Long reporterId, Long reportedId, Long bookingId);

    boolean existsByReporterIdAndReportedIdAndParcelId(
        Long reporterId, Long reportedId, Long parcelId);
}
