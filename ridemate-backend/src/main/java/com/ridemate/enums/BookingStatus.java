package com.ridemate.enums;

public enum BookingStatus {
    PENDING,    // Passenger requested
    APPROVED,   // Driver approved  → payment placeholder shown
    REJECTED,   // Driver rejected
    STARTED,    // Trip OTP verified → ride in progress
    COMPLETED,  // Ride finished
    CANCELLED   // Cancelled by passenger or system
}
