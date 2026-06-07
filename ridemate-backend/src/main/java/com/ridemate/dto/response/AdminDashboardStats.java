package com.ridemate.dto.response;

import lombok.*;

@Data @Builder
public class AdminDashboardStats {
    private long totalUsers;
    private long pendingVerifications;
    private long verifiedUsers;
    private long totalRides;
    private long totalParcels;
    private long openSosAlerts;
    private long openDisputes;
    private long openReports;
}
