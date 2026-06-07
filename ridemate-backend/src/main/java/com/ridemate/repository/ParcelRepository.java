package com.ridemate.repository;

import com.ridemate.entity.Parcel;
import com.ridemate.enums.ParcelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    /** All parcels sent by a user, newest first */
    List<Parcel> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    /** All parcel requests on a specific ride (driver view) */
    List<Parcel> findByRideIdOrderByCreatedAtDesc(Long rideId);

    /** Pending parcel requests on a specific ride (driver view — to-do list) */
    List<Parcel> findByRideIdAndStatus(Long rideId, ParcelStatus status);

    /**
     * All parcels on rides driven by a given user, across all statuses.
     * Used for the driver's "My Parcels" dashboard tab.
     */
    @Query("""
        SELECT p FROM Parcel p
        JOIN p.ride r
        WHERE r.driver.id = :driverId
        ORDER BY p.createdAt DESC
    """)
    List<Parcel> findByDriverId(Long driverId);

    /**
     * Pending parcel requests on rides driven by a given user.
     * Shown in the driver's notification badge.
     */
    @Query("""
        SELECT p FROM Parcel p
        JOIN p.ride r
        WHERE r.driver.id = :driverId AND p.status = 'PENDING'
        ORDER BY p.createdAt DESC
    """)
    List<Parcel> findPendingByDriverId(Long driverId);

    /** Check if a sender already has a non-rejected parcel on this ride */
    boolean existsByRideIdAndSenderIdAndStatusNot(Long rideId, Long senderId, ParcelStatus status);

    /**
     * Terminal parcels (DELIVERED or CANCELLED) on rides driven by the given user.
     * Used for the driver's parcel history view.
     */
    @Query("""
        SELECT p FROM Parcel p
        JOIN p.ride r
        WHERE r.driver.id = :driverId
          AND p.status IN :statuses
        ORDER BY p.createdAt DESC
    """)
    List<Parcel> findTerminalByDriverId(Long driverId, List<ParcelStatus> statuses);
}
