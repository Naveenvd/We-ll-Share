package com.ridemate.repository;

import com.ridemate.entity.Booking;
import com.ridemate.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** All bookings by a passenger, newest first */
    List<Booking> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    /** All bookings on rides owned by a driver, newest first */
    @Query("SELECT b FROM Booking b WHERE b.ride.driver.id = :driverId ORDER BY b.createdAt DESC")
    List<Booking> findByDriverId(Long driverId);

    /** Pending bookings that need driver action */
    @Query("SELECT b FROM Booking b WHERE b.ride.driver.id = :driverId AND b.status = 'PENDING'")
    List<Booking> findPendingByDriverId(Long driverId);

    /** All APPROVED bookings for a specific ride (used when starting ride) */
    List<Booking> findByRideIdAndStatus(Long rideId, BookingStatus status);

    /** Look up a booking by the public share token */
    Optional<Booking> findByTripShareToken(String token);

    /** Check if a passenger already has a non-rejected booking for this ride */
    boolean existsByRideIdAndPassengerIdAndStatusNot(Long rideId, Long passengerId, BookingStatus status);

    /**
     * All bookings on a ride whose status is one of the given values.
     * Used when cancelling a ride to bulk-cancel PENDING and APPROVED bookings.
     */
    List<Booking> findByRideIdAndStatusIn(Long rideId, List<BookingStatus> statuses);
}
