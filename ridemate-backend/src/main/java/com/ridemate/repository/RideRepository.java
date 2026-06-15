package com.ridemate.repository;

import com.ridemate.entity.Ride;
import com.ridemate.enums.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    /** All rides posted by a driver, newest first */
    List<Ride> findByDriverIdOrderByDepartureTimeDesc(Long driverId);

    /** Active (SCHEDULED) rides still available for booking */
    List<Ride> findByDriverIdAndStatusOrderByDepartureTimeDesc(Long driverId, RideStatus status);

    /**
     * Location-aware ride search using Haversine formula.
     *
     * Finds SCHEDULED rides where:
     *  - origin  is within :radiusKm km of (:fromLat, :fromLng)
     *  - destination is within :radiusKm km of (:toLat, :toLng)
     *  - departure date matches :date (ignoring time)
     *  - seatsAvailable >= :seats
     *  - optional womenOnly filter
     *  - optional pricePerSeat range
     *
     * Women-only rides are only returned when the requester is female
     * OR when womenOnly = false (caller handles gender filter at service layer).
     */
    @Query(value = """
        SELECT r.* FROM rides r
        WHERE r.status = 'SCHEDULED'
          AND r.accepts_passengers = true
          AND r.seats_available >= :seats
          AND CAST(r.departure_time AS date) = CAST(:date AS date)
          AND (
            6371 * ACOS(
              GREATEST(-1, LEAST(1,
                COS(RADIANS(:fromLat)) * COS(RADIANS(r.from_lat))
                * COS(RADIANS(r.from_lng) - RADIANS(:fromLng))
                + SIN(RADIANS(:fromLat)) * SIN(RADIANS(r.from_lat))
              ))
            )
          ) < :radiusKm
          AND (
            6371 * ACOS(
              GREATEST(-1, LEAST(1,
                COS(RADIANS(:toLat)) * COS(RADIANS(r.to_lat))
                * COS(RADIANS(r.to_lng) - RADIANS(:toLng))
                + SIN(RADIANS(:toLat)) * SIN(RADIANS(r.to_lat))
              ))
            )
          ) < :radiusKm
          AND (:womenOnly IS NULL OR r.women_only = :womenOnly)
          AND (:minPrice IS NULL OR r.price_per_seat >= :minPrice)
          AND (:maxPrice IS NULL OR r.price_per_seat <= :maxPrice)
        ORDER BY r.departure_time ASC
        """,
        countQuery = """
        SELECT COUNT(*) FROM rides r
        WHERE r.status = 'SCHEDULED'
          AND r.accepts_passengers = true
          AND r.seats_available >= :seats
          AND CAST(r.departure_time AS date) = CAST(:date AS date)
          AND (6371 * ACOS(GREATEST(-1, LEAST(1,
                COS(RADIANS(:fromLat)) * COS(RADIANS(r.from_lat))
                * COS(RADIANS(r.from_lng) - RADIANS(:fromLng))
                + SIN(RADIANS(:fromLat)) * SIN(RADIANS(r.from_lat)))))) < :radiusKm
          AND (6371 * ACOS(GREATEST(-1, LEAST(1,
                COS(RADIANS(:toLat)) * COS(RADIANS(r.to_lat))
                * COS(RADIANS(r.to_lng) - RADIANS(:toLng))
                + SIN(RADIANS(:toLat)) * SIN(RADIANS(r.to_lat)))))) < :radiusKm
          AND (:womenOnly IS NULL OR r.women_only = :womenOnly)
          AND (:minPrice IS NULL OR r.price_per_seat >= :minPrice)
          AND (:maxPrice IS NULL OR r.price_per_seat <= :maxPrice)
        """,
        nativeQuery = true)
    Page<Ride> searchRides(
        @Param("fromLat") BigDecimal fromLat,
        @Param("fromLng") BigDecimal fromLng,
        @Param("toLat")   BigDecimal toLat,
        @Param("toLng")   BigDecimal toLng,
        @Param("date")    LocalDateTime date,
        @Param("seats")   int seats,
        @Param("radiusKm") double radiusKm,
        @Param("womenOnly") Boolean womenOnly,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable);

    /**
     * Parcels-only search: SCHEDULED rides that accept parcels and
     * whose route overlaps the parcel from/to points.
     */
    @Query(value = """
        SELECT r.* FROM rides r
        WHERE r.status = 'SCHEDULED'
          AND r.accepts_parcels = true
          AND CAST(r.departure_time AS date) >= CAST(:fromDate AS date)
          AND (
            6371 * ACOS(GREATEST(-1, LEAST(1,
              COS(RADIANS(:fromLat)) * COS(RADIANS(r.from_lat))
              * COS(RADIANS(r.from_lng) - RADIANS(:fromLng))
              + SIN(RADIANS(:fromLat)) * SIN(RADIANS(r.from_lat)))))
          ) < :radiusKm
          AND (
            6371 * ACOS(GREATEST(-1, LEAST(1,
              COS(RADIANS(:toLat)) * COS(RADIANS(r.to_lat))
              * COS(RADIANS(r.to_lng) - RADIANS(:toLng))
              + SIN(RADIANS(:toLat)) * SIN(RADIANS(r.to_lat)))))
          ) < :radiusKm
        ORDER BY r.departure_time ASC
        """,
        countQuery = """
        SELECT COUNT(*) FROM rides r
        WHERE r.status = 'SCHEDULED'
          AND r.accepts_parcels = true
          AND CAST(r.departure_time AS date) >= CAST(:fromDate AS date)
          AND (6371 * ACOS(GREATEST(-1, LEAST(1,
                COS(RADIANS(:fromLat)) * COS(RADIANS(r.from_lat))
                * COS(RADIANS(r.from_lng) - RADIANS(:fromLng))
                + SIN(RADIANS(:fromLat)) * SIN(RADIANS(r.from_lat)))))) < :radiusKm
          AND (6371 * ACOS(GREATEST(-1, LEAST(1,
                COS(RADIANS(:toLat)) * COS(RADIANS(r.to_lat))
                * COS(RADIANS(r.to_lng) - RADIANS(:toLng))
                + SIN(RADIANS(:toLat)) * SIN(RADIANS(r.to_lat)))))) < :radiusKm
        """,
        nativeQuery = true)
    Page<Ride> searchRidesForParcel(
        @Param("fromLat") BigDecimal fromLat,
        @Param("fromLng") BigDecimal fromLng,
        @Param("toLat")   BigDecimal toLat,
        @Param("toLng")   BigDecimal toLng,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("radiusKm") double radiusKm,
        Pageable pageable);

    /** Admin — all rides paginated */
    Page<Ride> findAllByOrderByDepartureTimeDesc(Pageable pageable);
}
