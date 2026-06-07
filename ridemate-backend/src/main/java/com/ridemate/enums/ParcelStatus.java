package com.ridemate.enums;

/**
 * Lifecycle of a parcel delivery request:
 *
 *  PENDING  → sender posted, waiting for driver to accept
 *  ACCEPTED → driver accepted, pickup OTP generated & sent to sender
 *  IN_TRANSIT  → pickup OTP verified + before-photo taken by driver
 *  DELIVERED   → delivery OTP verified + after-photo taken by driver
 *  REJECTED    → driver rejected the request
 *  CANCELLED   → sender cancelled before acceptance
 *  COMPLAINT_RAISED → sender filed a complaint after pickup
 */
public enum ParcelStatus {
    PENDING,
    ACCEPTED,
    IN_TRANSIT,
    DELIVERED,
    REJECTED,
    CANCELLED,
    COMPLAINT_RAISED
}
