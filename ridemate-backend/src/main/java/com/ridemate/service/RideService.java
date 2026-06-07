package com.ridemate.service;

import com.ridemate.dto.request.RidePostRequest;
import com.ridemate.dto.response.*;
import com.ridemate.entity.*;
import com.ridemate.enums.*;
import com.ridemate.exception.AppException;
import com.ridemate.repository.*;
import com.ridemate.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository     rideRepository;
    private final BookingRepository  bookingRepository;
    private final UserRepository     userRepository;
    private final VehicleRepository  vehicleRepository;
    private final UserMapper         userMapper;

    /** Default search radius in kilometres */
    private static final double DEFAULT_RADIUS_KM = 30.0;

    // ── Post a ride ────────────────────────────────────────────────

    @Transactional
    public RideResponse postRide(String email, RidePostRequest req) {
        User driver = findVerifiedUser(email);

        // Only female drivers may create women-only rides
        if (req.isWomenOnly() && driver.getGender() != Gender.FEMALE) {
            throw new AppException(
                "Only female drivers can create women-only rides.", HttpStatus.FORBIDDEN);
        }

        // Must toggle at least one of passengers / parcels
        if (!req.isAcceptsPassengers() && !req.isAcceptsParcels()) {
            throw new AppException(
                "Ride must accept passengers, parcels, or both.", HttpStatus.BAD_REQUEST);
        }

        Vehicle vehicle = vehicleRepository.findByIdAndUserId(req.getVehicleId(), driver.getId())
            .orElseThrow(() -> new AppException("Vehicle not found.", HttpStatus.NOT_FOUND));

        Ride ride = Ride.builder()
            .driver(driver)
            .vehicle(vehicle)
            .fromLocation(req.getFromLocation())
            .toLocation(req.getToLocation())
            .fromLat(req.getFromLat()).fromLng(req.getFromLng())
            .toLat(req.getToLat()).toLng(req.getToLng())
            .departureTime(req.getDepartureTime())
            .seatsTotal(req.getSeatsTotal())
            .seatsAvailable(req.getSeatsTotal())
            .pricePerSeat(req.getPricePerSeat())
            .acceptsPassengers(req.isAcceptsPassengers())
            .acceptsParcels(req.isAcceptsParcels())
            .maxParcelSize(req.getMaxParcelSize())
            .womenOnly(req.isWomenOnly())
            .build();

        // Attach intermediate stops
        if (req.getStops() != null) {
            req.getStops().forEach(s -> {
                RideStop stop = RideStop.builder()
                    .ride(ride)
                    .stopName(s.getStopName())
                    .lat(s.getLat())
                    .lng(s.getLng())
                    .sequence(s.getSequence())
                    .build();
                ride.getStops().add(stop);
            });
        }

        return toResponse(rideRepository.save(ride));
    }

    // ── Get single ride ────────────────────────────────────────────

    public RideResponse getRide(Long rideId, String requestorEmail) {
        Ride ride = findRide(rideId);
        // Hide women-only rides from non-female users
        if (ride.isWomenOnly()) {
            User u = findUser(requestorEmail);
            if (u.getGender() != Gender.FEMALE) {
                throw new AppException("Ride not found.", HttpStatus.NOT_FOUND);
            }
        }
        return toResponse(ride);
    }

    // ── Driver's own rides ─────────────────────────────────────────

    public List<RideResponse> getMyPostedRides(String email) {
        User driver = findUser(email);
        return rideRepository.findByDriverIdOrderByDepartureTimeDesc(driver.getId())
            .stream().map(this::toResponse).toList();
    }

    // ── Cancel a ride ──────────────────────────────────────────────

    @Transactional
    public RideResponse cancelRide(Long rideId, String email) {
        User driver = findUser(email);
        Ride ride   = findRide(rideId);

        if (!ride.getDriver().getId().equals(driver.getId())) {
            throw new AppException("You are not the driver of this ride.", HttpStatus.FORBIDDEN);
        }
        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new AppException("Only SCHEDULED rides can be cancelled.", HttpStatus.BAD_REQUEST);
        }

        // B6 fix: cascade-cancel all PENDING and APPROVED bookings on this ride
        List<Booking> active = bookingRepository.findByRideIdAndStatusIn(
            rideId, List.of(BookingStatus.PENDING, BookingStatus.APPROVED));

        active.forEach(b -> {
            // Restore seats reserved by APPROVED bookings
            if (b.getStatus() == BookingStatus.APPROVED) {
                ride.setSeatsAvailable(ride.getSeatsAvailable() + b.getSeatsBooked());
            }
            b.setStatus(BookingStatus.CANCELLED);
        });
        if (!active.isEmpty()) {
            bookingRepository.saveAll(active);
        }

        ride.setStatus(RideStatus.CANCELLED);
        return toResponse(rideRepository.save(ride));
    }

    // ── Search rides (passenger) ───────────────────────────────────

    public Page<RideResponse> searchRides(
            String requestorEmail,
            BigDecimal fromLat, BigDecimal fromLng,
            BigDecimal toLat,   BigDecimal toLng,
            LocalDateTime date,
            int seats,
            BigDecimal minPrice, BigDecimal maxPrice,
            Boolean womenOnly,
            int page, int size) {

        User requestor = findUser(requestorEmail);

        // Non-female users cannot see women-only rides; override their filter to false
        Boolean womenOnlyFilter = womenOnly;
        if (requestor.getGender() != Gender.FEMALE) {
            womenOnlyFilter = false;   // force exclude women-only
        }

        Pageable pageable = PageRequest.of(page, size);

        return rideRepository.searchRides(
            fromLat, fromLng, toLat, toLng,
            date, seats,
            DEFAULT_RADIUS_KM,
            womenOnlyFilter, minPrice, maxPrice,
            pageable
        ).map(this::toResponse);
    }

    // ── Mapper ─────────────────────────────────────────────────────

    public RideResponse toResponse(Ride r) {
        List<RideStopResponse> stops = r.getStops() == null ? List.of() :
            r.getStops().stream()
                .sorted((a, b) -> Integer.compare(a.getSequence(), b.getSequence()))
                .map(s -> RideStopResponse.builder()
                    .id(s.getId()).stopName(s.getStopName())
                    .lat(s.getLat()).lng(s.getLng()).sequence(s.getSequence())
                    .build())
                .toList();

        VehicleResponse vr = null;
        if (r.getVehicle() != null) {
            Vehicle v = r.getVehicle();
            vr = VehicleResponse.builder()
                .id(v.getId()).model(v.getModel())
                .numberPlate(v.getNumberPlate()).color(v.getColor())
                .seats(v.getSeats()).createdAt(v.getCreatedAt())
                .build();
        }

        User d = r.getDriver();
        DriverSummaryResponse driverSummary = DriverSummaryResponse.builder()
            .id(d.getId()).name(d.getName())
            .photoUrl(userMapper.toUrl(d.getPhotoPath()))
            .gender(d.getGender())
            .avgRating(d.getAvgRating())
            .totalRides(d.getTotalRides())
            .build();

        return RideResponse.builder()
            .id(r.getId())
            .driver(driverSummary)
            .vehicle(vr)
            .fromLocation(r.getFromLocation()).toLocation(r.getToLocation())
            .fromLat(r.getFromLat()).fromLng(r.getFromLng())
            .toLat(r.getToLat()).toLng(r.getToLng())
            .departureTime(r.getDepartureTime())
            .seatsTotal(r.getSeatsTotal()).seatsAvailable(r.getSeatsAvailable())
            .pricePerSeat(r.getPricePerSeat())
            .acceptsPassengers(r.isAcceptsPassengers())
            .acceptsParcels(r.isAcceptsParcels())
            .maxParcelSize(r.getMaxParcelSize())
            .womenOnly(r.isWomenOnly())
            .status(r.getStatus())
            .stops(stops)
            .createdAt(r.getCreatedAt())
            .build();
    }

    // ── Private helpers ────────────────────────────────────────────

    private User findVerifiedUser(String email) {
        User u = findUser(email);
        if (u.getStatus() != UserStatus.VERIFIED) {
            throw new AppException(
                "Your account must be verified before you can post rides.", HttpStatus.FORBIDDEN);
        }
        return u;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }

    public Ride findRide(Long rideId) {
        return rideRepository.findById(rideId)
            .orElseThrow(() -> new AppException("Ride not found.", HttpStatus.NOT_FOUND));
    }
}
