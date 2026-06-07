package com.ridemate.service;

import com.ridemate.dto.request.BookingRequest;
import com.ridemate.dto.response.*;
import com.ridemate.entity.*;
import com.ridemate.enums.*;
import com.ridemate.exception.AppException;
import com.ridemate.repository.*;
import com.ridemate.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository  bookingRepository;
    private final RideRepository     rideRepository;
    private final UserRepository     userRepository;
    private final MessageRepository  messageRepository;
    private final RideService        rideService;
    private final UserMapper         userMapper;

    private static final SecureRandom RANDOM = new SecureRandom();

    // ── Passenger: request a booking ──────────────────────────────

    @Transactional
    public BookingResponse requestBooking(String passengerEmail, BookingRequest req) {
        User passenger = findVerifiedUser(passengerEmail);
        Ride ride = rideService.findRide(req.getRideId());

        // Business rule checks
        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new AppException("This ride is no longer available.", HttpStatus.BAD_REQUEST);
        }
        if (!ride.isAcceptsPassengers()) {
            throw new AppException("This ride does not accept passengers.", HttpStatus.BAD_REQUEST);
        }
        if (ride.isWomenOnly() && passenger.getGender() != Gender.FEMALE) {
            throw new AppException("This is a women-only ride.", HttpStatus.FORBIDDEN);
        }
        if (ride.getDriver().getId().equals(passenger.getId())) {
            throw new AppException("You cannot book your own ride.", HttpStatus.BAD_REQUEST);
        }
        if (ride.getSeatsAvailable() < req.getSeatsBooked()) {
            throw new AppException(
                "Only " + ride.getSeatsAvailable() + " seat(s) available.", HttpStatus.BAD_REQUEST);
        }
        if (bookingRepository.existsByRideIdAndPassengerIdAndStatusNot(
                ride.getId(), passenger.getId(), BookingStatus.REJECTED)) {
            throw new AppException("You already have a booking for this ride.", HttpStatus.CONFLICT);
        }

        BigDecimal amount = ride.getPricePerSeat()
            .multiply(BigDecimal.valueOf(req.getSeatsBooked()));

        Booking booking = Booking.builder()
            .ride(ride)
            .passenger(passenger)
            .seatsBooked(req.getSeatsBooked())
            .amount(amount)
            .build();

        return toResponse(bookingRepository.save(booking), passengerEmail);
    }

    // ── Driver: approve / reject ───────────────────────────────────

    @Transactional
    public BookingResponse approveBooking(Long bookingId, String driverEmail) {
        Booking booking = findBookingForDriver(bookingId, driverEmail);
        assertStatus(booking, BookingStatus.PENDING);

        Ride ride = booking.getRide();
        if (ride.getSeatsAvailable() < booking.getSeatsBooked()) {
            throw new AppException("Not enough seats available.", HttpStatus.BAD_REQUEST);
        }

        // Deduct seats
        ride.setSeatsAvailable(ride.getSeatsAvailable() - booking.getSeatsBooked());
        rideRepository.save(ride);

        // Generate trip share token
        booking.setStatus(BookingStatus.APPROVED);
        booking.setTripShareToken(UUID.randomUUID().toString().replace("-", ""));

        return toResponse(bookingRepository.save(booking), null);
    }

    @Transactional
    public BookingResponse rejectBooking(Long bookingId, String driverEmail) {
        Booking booking = findBookingForDriver(bookingId, driverEmail);
        assertStatus(booking, BookingStatus.PENDING);
        booking.setStatus(BookingStatus.REJECTED);
        return toResponse(bookingRepository.save(booking), null);
    }

    // ── Passenger: cancel ─────────────────────────────────────────

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String passengerEmail) {
        User passenger = findUser(passengerEmail);
        Booking booking = findBooking(bookingId);

        if (!booking.getPassenger().getId().equals(passenger.getId())) {
            throw new AppException("Not your booking.", HttpStatus.FORBIDDEN);
        }
        if (booking.getStatus() == BookingStatus.STARTED ||
            booking.getStatus() == BookingStatus.COMPLETED) {
            throw new AppException("Cannot cancel a ride already in progress.", HttpStatus.BAD_REQUEST);
        }

        // Restore seats if was APPROVED
        if (booking.getStatus() == BookingStatus.APPROVED) {
            Ride ride = booking.getRide();
            ride.setSeatsAvailable(ride.getSeatsAvailable() + booking.getSeatsBooked());
            rideRepository.save(ride);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking), passengerEmail);
    }

    // ── Driver: start ride → generate trip OTPs ───────────────────

    @Transactional
    public List<BookingResponse> startRide(Long rideId, String driverEmail) {
        User driver = findUser(driverEmail);
        Ride ride   = rideService.findRide(rideId);

        if (!ride.getDriver().getId().equals(driver.getId())) {
            throw new AppException("Not your ride.", HttpStatus.FORBIDDEN);
        }
        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new AppException("Ride is not in SCHEDULED state.", HttpStatus.BAD_REQUEST);
        }

        ride.setStatus(RideStatus.STARTED);
        rideRepository.save(ride);

        // Generate 4-digit OTP for every APPROVED booking
        List<Booking> approved = bookingRepository.findByRideIdAndStatus(
            rideId, BookingStatus.APPROVED);

        approved.forEach(b -> {
            String otp = String.format("%04d", RANDOM.nextInt(10_000));
            b.setTripOtp(otp);
            log.info("╔══════════════════════════════════════╗");
            log.info("║ [TRIP OTP] Booking #{} | Passenger: {} | OTP: {} ║",
                b.getId(), b.getPassenger().getName(), otp);
            log.info("╚══════════════════════════════════════╝");
        });
        bookingRepository.saveAll(approved);

        return approved.stream()
            .map(b -> toResponse(b, null))
            .toList();
    }

    // ── Driver: verify OTP at pickup ──────────────────────────────

    @Transactional
    public BookingResponse verifyTripOtp(Long bookingId, String driverEmail, String otp) {
        Booking booking = findBookingForDriver(bookingId, driverEmail);
        assertStatus(booking, BookingStatus.APPROVED);

        if (booking.getTripOtp() == null) {
            throw new AppException(
                "Trip OTP has not been generated yet. Start the ride first.", HttpStatus.BAD_REQUEST);
        }
        if (!booking.getTripOtp().equals(otp)) {
            throw new AppException("Invalid OTP. Please ask the passenger again.", HttpStatus.BAD_REQUEST);
        }

        booking.setOtpVerified(true);
        booking.setStatus(BookingStatus.STARTED);
        return toResponse(bookingRepository.save(booking), null);
    }

    // ── Mark booking COMPLETED ────────────────────────────────────

    @Transactional
    public BookingResponse completeBooking(Long bookingId, String driverEmail) {
        Booking booking = findBookingForDriver(bookingId, driverEmail);
        assertStatus(booking, BookingStatus.STARTED);
        booking.setStatus(BookingStatus.COMPLETED);

        // Increment driver's total ride count
        User driver = booking.getRide().getDriver();
        driver.setTotalRides(driver.getTotalRides() + 1);
        userRepository.save(driver);

        return toResponse(bookingRepository.save(booking), null);
    }

    // ── View bookings ─────────────────────────────────────────────

    public List<BookingResponse> getMyBookings(String email) {
        User user = findUser(email);
        return bookingRepository.findByPassengerIdOrderByCreatedAtDesc(user.getId())
            .stream().map(b -> toResponse(b, email)).toList();
    }

    public List<BookingResponse> getDriverBookings(String email) {
        User driver = findUser(email);
        return bookingRepository.findByDriverId(driver.getId())
            .stream().map(b -> toResponse(b, null)).toList();
    }

    public List<BookingResponse> getPendingDriverBookings(String email) {
        User driver = findUser(email);
        return bookingRepository.findPendingByDriverId(driver.getId())
            .stream().map(b -> toResponse(b, null)).toList();
    }

    public BookingResponse getBooking(Long bookingId, String email) {
        User user     = findUser(email);
        Booking booking = findBooking(bookingId);

        boolean isPassenger = booking.getPassenger().getId().equals(user.getId());
        boolean isDriver    = booking.getRide().getDriver().getId().equals(user.getId());

        if (!isPassenger && !isDriver) {
            throw new AppException("Access denied.", HttpStatus.FORBIDDEN);
        }
        return toResponse(booking, isPassenger ? email : null);
    }

    /** Public trip-share: no auth required */
    public BookingResponse getBookingByShareToken(String token) {
        Booking b = bookingRepository.findByTripShareToken(token)
            .orElseThrow(() -> new AppException("Tracking link not found.", HttpStatus.NOT_FOUND));
        return toResponse(b, null);
    }

    // ── Mapper ─────────────────────────────────────────────────────

    private BookingResponse toResponse(Booking b, String viewerEmail) {
        User p = b.getPassenger();
        PassengerSummaryResponse passengerDto = PassengerSummaryResponse.builder()
            .id(p.getId()).name(p.getName())
            .photoUrl(userMapper.toUrl(p.getPhotoPath()))
            .gender(p.getGender())
            .avgRating(p.getAvgRating()).totalRides(p.getTotalRides())
            .build();

        // OTP only shown to the passenger once the ride is started (status APPROVED or STARTED)
        // Parentheses are critical — || has lower precedence than &&
        String otp = null;
        if (viewerEmail != null
                && p.getEmail().equals(viewerEmail)
                && (b.getStatus() == BookingStatus.APPROVED
                    || b.getStatus() == BookingStatus.STARTED)) {
            otp = b.getTripOtp();
        }

        long unread = 0;
        if (viewerEmail != null) {
            User viewer = userRepository.findByEmail(viewerEmail).orElse(null);
            if (viewer != null) {
                unread = messageRepository.countByBookingIdAndReadFalseAndSenderIdNot(
                    b.getId(), viewer.getId());
            }
        }

        return BookingResponse.builder()
            .id(b.getId())
            .ride(rideService.toResponse(b.getRide()))
            .passenger(passengerDto)
            .seatsBooked(b.getSeatsBooked())
            .status(b.getStatus())
            .tripOtp(otp)
            .otpVerified(b.isOtpVerified())
            .tripShareToken(b.getTripShareToken())
            .amount(b.getAmount())
            .createdAt(b.getCreatedAt())
            .unreadMessages(unread)
            .build();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Booking findBookingForDriver(Long bookingId, String driverEmail) {
        User driver   = findUser(driverEmail);
        Booking booking = findBooking(bookingId);
        if (!booking.getRide().getDriver().getId().equals(driver.getId())) {
            throw new AppException("Not your booking to manage.", HttpStatus.FORBIDDEN);
        }
        return booking;
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new AppException("Booking not found.", HttpStatus.NOT_FOUND));
    }

    private User findVerifiedUser(String email) {
        User u = findUser(email);
        if (u.getStatus() != UserStatus.VERIFIED) {
            throw new AppException("Your account must be verified.", HttpStatus.FORBIDDEN);
        }
        return u;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }

    private void assertStatus(Booking b, BookingStatus expected) {
        if (b.getStatus() != expected) {
            throw new AppException(
                "Booking must be in " + expected + " state. Current: " + b.getStatus(),
                HttpStatus.BAD_REQUEST);
        }
    }
}
