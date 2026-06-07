package com.ridemate.util;

import com.ridemate.dto.response.AdminUserSummary;
import com.ridemate.dto.response.UserProfileResponse;
import com.ridemate.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converts User entities to DTOs.
 * Builds file-serving URLs using the /api/files/{subDir}/{filename} endpoint.
 */
@Component
public class UserMapper {

    private static final String FILE_BASE = "http://localhost:8080/api/files/";

    public UserProfileResponse toProfile(User u) {
        return UserProfileResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .gender(u.getGender())
                .dob(u.getDob())
                .role(u.getRole())
                .status(u.getStatus())
                .rejectionReason(u.getRejectionReason())
                .photoUrl(toUrl(u.getPhotoPath()))
                .aadhaarNumber(u.getAadhaarNumber())
                .aadhaarDocUrl(toUrl(u.getAadhaarDocPath()))
                .dlNumber(u.getDlNumber())
                .dlDocUrl(toUrl(u.getDlDocPath()))
                .phoneVerified(u.isPhoneVerified())
                .avgRating(u.getAvgRating())
                .totalRides(u.getTotalRides())
                .totalParcelsDelivered(u.getTotalParcelsDelivered())
                .createdAt(u.getCreatedAt())
                .build();
    }

    public AdminUserSummary toAdminSummary(User u) {
        return AdminUserSummary.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .gender(u.getGender())
                .status(u.getStatus())
                .rejectionReason(u.getRejectionReason())
                .phoneVerified(u.isPhoneVerified())
                .aadhaarNumber(u.getAadhaarNumber())
                .aadhaarDocUrl(toUrl(u.getAadhaarDocPath()))
                .dlNumber(u.getDlNumber())
                .dlDocUrl(toUrl(u.getDlDocPath()))
                .photoUrl(toUrl(u.getPhotoPath()))
                .createdAt(u.getCreatedAt())
                .build();
    }

    /** Converts a relative stored path to a full serving URL, or null if path is absent */
    public String toUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return null;
        // relativePath is like "aadhaar/uuid.pdf" → URL is /api/files/aadhaar/uuid.pdf
        return FILE_BASE + relativePath;
    }
}
