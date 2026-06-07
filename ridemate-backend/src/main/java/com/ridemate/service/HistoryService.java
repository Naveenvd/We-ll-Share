package com.ridemate.service;

import com.ridemate.dto.response.HistoryItemResponse;
import com.ridemate.entity.*;
import com.ridemate.enums.*;
import com.ridemate.exception.AppException;
import com.ridemate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Aggregates completed / cancelled activity across rides (driver view),
 * bookings (passenger view), and parcels (sender view) into a unified
 * history list sorted newest-first by event time.
 */
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final UserRepository    userRepository;
    private final RideRepository    rideRepository;
    private final BookingRepository bookingRepository;
    private final ParcelRepository  parcelRepository;

    /** Only these statuses are considered "terminal" (history-worthy). */
    private static final Set<RideStatus>    TERMINAL_RIDES =
            Set.of(RideStatus.COMPLETED, RideStatus.CANCELLED);

    private static final Set<BookingStatus> TERMINAL_BOOKINGS =
            Set.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.REJECTED);

    private static final Set<ParcelStatus>  TERMINAL_PARCELS =
            Set.of(ParcelStatus.DELIVERED, ParcelStatus.CANCELLED);

    // ─────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HistoryItemResponse> getHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));

        List<HistoryItemResponse> items = new ArrayList<>();

        // 1. Rides posted by this user (driver perspective)
        rideRepository.findByDriverIdOrderByDepartureTimeDesc(user.getId())
                .stream()
                .filter(r -> TERMINAL_RIDES.contains(r.getStatus()))
                .map(this::toDriverRideItem)
                .forEach(items::add);

        // 2. Bookings made by this user (passenger perspective)
        bookingRepository.findByPassengerIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .filter(b -> TERMINAL_BOOKINGS.contains(b.getStatus()))
                .map(this::toPassengerBookingItem)
                .forEach(items::add);

        // 3. Parcels sent by this user (sender perspective)
        parcelRepository.findBySenderIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .filter(p -> TERMINAL_PARCELS.contains(p.getStatus()))
                .map(this::toSenderParcelItem)
                .forEach(items::add);

        // 4. F3 fix: Parcels carried by this user as driver (driver perspective)
        parcelRepository.findTerminalByDriverId(user.getId(), List.copyOf(TERMINAL_PARCELS))
                .stream()
                .map(this::toDriverParcelItem)
                .forEach(items::add);

        // Sort all items newest-first; null eventTimes go to the end
        items.sort(Comparator.comparing(
                HistoryItemResponse::getEventTime,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return items;
    }

    // ── Private mappers ───────────────────────────────────────────────────

    /**
     * Driver's ride history item.
     * Earnings = sum of amount from COMPLETED bookings on that ride.
     */
    private HistoryItemResponse toDriverRideItem(Ride r) {
        BigDecimal earnings = bookingRepository
                .findByRideIdAndStatus(r.getId(), BookingStatus.COMPLETED)
                .stream()
                .map(b -> b.getAmount() != null ? b.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return HistoryItemResponse.builder()
                .id(r.getId())
                .type(HistoryItemResponse.ItemType.RIDE_DRIVER)
                .status(r.getStatus().name())
                .fromLocation(r.getFromLocation())
                .toLocation(r.getToLocation())
                .eventTime(r.getDepartureTime())
                .earnings(earnings)
                .build();
    }

    /**
     * Passenger's booking history item.
     * Counter-party is the ride's driver.
     */
    private HistoryItemResponse toPassengerBookingItem(Booking b) {
        Ride ride   = b.getRide();
        User driver = ride.getDriver();

        return HistoryItemResponse.builder()
                .id(b.getId())
                .type(HistoryItemResponse.ItemType.RIDE_PASSENGER)
                .status(b.getStatus().name())
                .fromLocation(ride.getFromLocation())
                .toLocation(ride.getToLocation())
                .eventTime(ride.getDepartureTime())
                .amount(b.getAmount())
                .counterPartyName(driver.getName())
                .build();
    }

    /**
     * Sender's parcel history item.
     * Counter-party is the ride's driver who carried the parcel.
     * Event time is the parcel creation time (ride's departure may be in future
     * for CANCELLED parcels).
     */
    private HistoryItemResponse toSenderParcelItem(Parcel p) {
        User driver = p.getRide().getDriver();

        return HistoryItemResponse.builder()
                .id(p.getId())
                .type(HistoryItemResponse.ItemType.PARCEL_SENDER)
                .status(p.getStatus().name())
                .fromLocation(p.getFromLocation())
                .toLocation(p.getToLocation())
                .eventTime(p.getCreatedAt())
                .price(p.getPrice())
                .counterPartyName(driver.getName())
                .build();
    }

    /**
     * F3: Driver's parcel history item.
     * Represents a parcel the user carried (or cancelled) on their ride.
     * Counter-party is the parcel sender.
     */
    private HistoryItemResponse toDriverParcelItem(Parcel p) {
        User sender = p.getSender();

        return HistoryItemResponse.builder()
                .id(p.getId())
                .type(HistoryItemResponse.ItemType.PARCEL_DRIVER)
                .status(p.getStatus().name())
                .fromLocation(p.getFromLocation())
                .toLocation(p.getToLocation())
                .eventTime(p.getCreatedAt())
                .price(p.getPrice())           // same price field — what the sender offered
                .counterPartyName(sender.getName())
                .build();
    }
}
