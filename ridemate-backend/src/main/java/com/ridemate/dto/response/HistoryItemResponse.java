package com.ridemate.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unified history item representing a completed/cancelled ride (as driver
 * or passenger) or a delivered/cancelled parcel (as sender).
 *
 * The "type" field distinguishes the three variants.
 */
@Data @Builder
public class HistoryItemResponse {

    public enum ItemType { RIDE_DRIVER, RIDE_PASSENGER, PARCEL_SENDER, PARCEL_DRIVER }

    private Long          id;
    private ItemType      type;
    private String        status;

    private String        fromLocation;
    private String        toLocation;
    private LocalDateTime eventTime;   // departure time for rides, createdAt for parcels

    /** For RIDE_DRIVER: total earnings from completed bookings on this ride */
    private BigDecimal    earnings;

    /** For RIDE_PASSENGER: booking amount paid */
    private BigDecimal    amount;

    /** For PARCEL_SENDER: parcel price offered */
    private BigDecimal    price;

    /** Counter-party display name */
    private String        counterPartyName;
    private String        counterPartyPhotoUrl;
}
