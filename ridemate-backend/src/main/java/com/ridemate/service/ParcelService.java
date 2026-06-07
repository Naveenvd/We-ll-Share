package com.ridemate.service;

import com.ridemate.dto.request.ParcelComplaintRequest;
import com.ridemate.dto.request.ParcelPostRequest;
import com.ridemate.dto.response.ParcelComplaintResponse;
import com.ridemate.dto.response.ParcelResponse;
import com.ridemate.dto.response.PassengerSummaryResponse;
import com.ridemate.entity.*;
import com.ridemate.enums.ParcelStatus;
import com.ridemate.enums.RideStatus;
import com.ridemate.exception.AppException;
import com.ridemate.repository.*;
import com.ridemate.util.FileStorageUtil;
import com.ridemate.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParcelService {

    private final ParcelRepository          parcelRepository;
    private final ParcelComplaintRepository complaintRepository;
    private final UserRepository            userRepository;
    private final MessageRepository         messageRepository;
    private final RideService               rideService;
    private final FileStorageUtil           fileStorageUtil;
    private final UserMapper                userMapper;

    private static final SecureRandom RANDOM = new SecureRandom();

    // ── Sender: post a parcel ────────────────────────────────────────

    @Transactional
    public ParcelResponse postParcel(String senderEmail,
                                     ParcelPostRequest req,
                                     MultipartFile photo) {
        User sender = findVerifiedUser(senderEmail);
        Ride ride   = rideService.findRide(req.getRideId());

        // Business rule checks
        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new AppException("This ride is no longer available.", HttpStatus.BAD_REQUEST);
        }
        if (!ride.isAcceptsParcels()) {
            throw new AppException("This ride does not accept parcels.", HttpStatus.BAD_REQUEST);
        }
        if (ride.getDriver().getId().equals(sender.getId())) {
            throw new AppException("You cannot send a parcel on your own ride.", HttpStatus.BAD_REQUEST);
        }
        if (ride.getMaxParcelSize() != null &&
                req.getSize().ordinal() > ride.getMaxParcelSize().ordinal()) {
            throw new AppException(
                "This ride only accepts up to " + ride.getMaxParcelSize() + " parcels.",
                HttpStatus.BAD_REQUEST);
        }
        if (parcelRepository.existsByRideIdAndSenderIdAndStatusNot(
                ride.getId(), sender.getId(), ParcelStatus.REJECTED)) {
            throw new AppException("You already have a parcel request on this ride.", HttpStatus.CONFLICT);
        }

        String photoPath = null;
        if (photo != null && !photo.isEmpty()) {
            photoPath = fileStorageUtil.storeImage(photo, "parcels");
        }

        Parcel parcel = Parcel.builder()
            .sender(sender)
            .ride(ride)
            .fromLocation(req.getFromLocation())
            .fromLat(req.getFromLat())
            .fromLng(req.getFromLng())
            .toLocation(req.getToLocation())
            .toLat(req.getToLat())
            .toLng(req.getToLng())
            .size(req.getSize())
            .description(req.getDescription())
            .photoPath(photoPath)
            .price(req.getPrice())
            .restrictedItemsAcknowledged(req.isRestrictedItemsAcknowledged())
            .build();

        return toResponse(parcelRepository.save(parcel), senderEmail);
    }

    // ── Driver: accept ────────────────────────────────────────────────

    @Transactional
    public ParcelResponse acceptParcel(Long parcelId, String driverEmail) {
        Parcel parcel = findParcelForDriver(parcelId, driverEmail);
        assertParcelStatus(parcel, ParcelStatus.PENDING);

        String otp = String.format("%04d", RANDOM.nextInt(10_000));
        parcel.setStatus(ParcelStatus.ACCEPTED);
        parcel.setPickupOtp(otp);

        log.info("╔══════════════════════════════════════╗");
        log.info("║ [PICKUP OTP] Parcel #{} | Sender: {} | OTP: {} ║",
            parcel.getId(), parcel.getSender().getName(), otp);
        log.info("╚══════════════════════════════════════╝");

        return toResponse(parcelRepository.save(parcel), null);
    }

    // ── Driver: reject ────────────────────────────────────────────────

    @Transactional
    public ParcelResponse rejectParcel(Long parcelId, String driverEmail) {
        Parcel parcel = findParcelForDriver(parcelId, driverEmail);
        assertParcelStatus(parcel, ParcelStatus.PENDING);
        parcel.setStatus(ParcelStatus.REJECTED);
        return toResponse(parcelRepository.save(parcel), null);
    }

    // ── Sender: cancel ────────────────────────────────────────────────

    @Transactional
    public ParcelResponse cancelParcel(Long parcelId, String senderEmail) {
        User   sender = findUser(senderEmail);
        Parcel parcel = findParcel(parcelId);

        if (!parcel.getSender().getId().equals(sender.getId())) {
            throw new AppException("Not your parcel.", HttpStatus.FORBIDDEN);
        }
        if (parcel.getStatus() == ParcelStatus.IN_TRANSIT ||
            parcel.getStatus() == ParcelStatus.DELIVERED) {
            throw new AppException("Cannot cancel a parcel already in transit.", HttpStatus.BAD_REQUEST);
        }

        parcel.setStatus(ParcelStatus.CANCELLED);
        return toResponse(parcelRepository.save(parcel), senderEmail);
    }

    // ── Driver: verify pickup OTP + upload before-photo ───────────────

    @Transactional
    public ParcelResponse verifyPickupOtp(Long parcelId,
                                          String driverEmail,
                                          String otp,
                                          MultipartFile beforePhoto) {
        Parcel parcel = findParcelForDriver(parcelId, driverEmail);
        assertParcelStatus(parcel, ParcelStatus.ACCEPTED);

        if (parcel.getPickupOtp() == null) {
            throw new AppException("Pickup OTP not yet generated.", HttpStatus.BAD_REQUEST);
        }
        if (!parcel.getPickupOtp().equals(otp)) {
            throw new AppException("Invalid pickup OTP.", HttpStatus.BAD_REQUEST);
        }

        if (beforePhoto != null && !beforePhoto.isEmpty()) {
            parcel.setBeforePhotoPath(fileStorageUtil.storeImage(beforePhoto, "parcels"));
        }

        // Generate delivery OTP now (shown to sender once IN_TRANSIT)
        String deliveryOtp = String.format("%04d", RANDOM.nextInt(10_000));
        parcel.setDeliveryOtp(deliveryOtp);

        parcel.setPickupOtpVerified(true);
        parcel.setStatus(ParcelStatus.IN_TRANSIT);

        log.info("╔══════════════════════════════════════╗");
        log.info("║ [DELIVERY OTP] Parcel #{} | Sender: {} | OTP: {} ║",
            parcel.getId(), parcel.getSender().getName(), deliveryOtp);
        log.info("╚══════════════════════════════════════╝");

        return toResponse(parcelRepository.save(parcel), null);
    }

    // ── Driver: verify delivery OTP + upload after-photo ─────────────

    @Transactional
    public ParcelResponse verifyDeliveryOtp(Long parcelId,
                                             String driverEmail,
                                             String otp,
                                             MultipartFile afterPhoto) {
        Parcel parcel = findParcelForDriver(parcelId, driverEmail);
        assertParcelStatus(parcel, ParcelStatus.IN_TRANSIT);

        if (parcel.getDeliveryOtp() == null) {
            throw new AppException("Delivery OTP not yet generated.", HttpStatus.BAD_REQUEST);
        }
        if (!parcel.getDeliveryOtp().equals(otp)) {
            throw new AppException("Invalid delivery OTP.", HttpStatus.BAD_REQUEST);
        }

        if (afterPhoto != null && !afterPhoto.isEmpty()) {
            parcel.setAfterPhotoPath(fileStorageUtil.storeImage(afterPhoto, "parcels"));
        }

        parcel.setDeliveryOtpVerified(true);
        parcel.setStatus(ParcelStatus.DELIVERED);

        // B8 fix: increment the driver's totalParcelsDelivered counter
        User driver = parcel.getRide().getDriver();
        driver.setTotalParcelsDelivered(driver.getTotalParcelsDelivered() + 1);
        userRepository.save(driver);

        return toResponse(parcelRepository.save(parcel), null);
    }

    // ── Raise a complaint ─────────────────────────────────────────────

    @Transactional
    public ParcelComplaintResponse raiseComplaint(Long parcelId,
                                                  String userEmail,
                                                  ParcelComplaintRequest req) {
        User   user   = findUser(userEmail);
        Parcel parcel = findParcel(parcelId);

        boolean isSender = parcel.getSender().getId().equals(user.getId());
        boolean isDriver = parcel.getRide().getDriver().getId().equals(user.getId());

        if (!isSender && !isDriver) {
            throw new AppException("You are not a party in this parcel.", HttpStatus.FORBIDDEN);
        }
        if (parcel.getStatus() == ParcelStatus.PENDING ||
            parcel.getStatus() == ParcelStatus.REJECTED ||
            parcel.getStatus() == ParcelStatus.CANCELLED) {
            throw new AppException("Cannot raise a complaint for a parcel in " +
                parcel.getStatus() + " state.", HttpStatus.BAD_REQUEST);
        }
        if (complaintRepository.existsByParcelId(parcelId)) {
            throw new AppException("A complaint already exists for this parcel.", HttpStatus.CONFLICT);
        }

        parcel.setStatus(ParcelStatus.COMPLAINT_RAISED);
        parcelRepository.save(parcel);

        ParcelComplaint complaint = ParcelComplaint.builder()
            .parcel(parcel)
            .raisedBy(user)
            .reason(req.getReason())
            .build();

        return toComplaintResponse(complaintRepository.save(complaint));
    }

    // ── View: sender ──────────────────────────────────────────────────

    public List<ParcelResponse> getMySentParcels(String email) {
        User user = findUser(email);
        return parcelRepository.findBySenderIdOrderByCreatedAtDesc(user.getId())
            .stream().map(p -> toResponse(p, email)).toList();
    }

    // ── View: driver ──────────────────────────────────────────────────

    public List<ParcelResponse> getMyCarryingParcels(String email) {
        User driver = findUser(email);
        return parcelRepository.findByDriverId(driver.getId())
            .stream().map(p -> toResponse(p, null)).toList();
    }

    public List<ParcelResponse> getPendingParcelsForDriver(String email) {
        User driver = findUser(email);
        return parcelRepository.findPendingByDriverId(driver.getId())
            .stream().map(p -> toResponse(p, null)).toList();
    }

    /** All parcel requests on a specific ride (driver view) */
    public List<ParcelResponse> getParcelsForRide(Long rideId, String driverEmail) {
        User driver = findUser(driverEmail);
        Ride ride   = rideService.findRide(rideId);
        if (!ride.getDriver().getId().equals(driver.getId())) {
            throw new AppException("Not your ride.", HttpStatus.FORBIDDEN);
        }
        return parcelRepository.findByRideIdOrderByCreatedAtDesc(rideId)
            .stream().map(p -> toResponse(p, null)).toList();
    }

    // ── Single parcel ─────────────────────────────────────────────────

    public ParcelResponse getParcel(Long parcelId, String email) {
        User   user   = findUser(email);
        Parcel parcel = findParcel(parcelId);

        boolean isSender = parcel.getSender().getId().equals(user.getId());
        boolean isDriver = parcel.getRide().getDriver().getId().equals(user.getId());

        if (!isSender && !isDriver) {
            throw new AppException("Access denied.", HttpStatus.FORBIDDEN);
        }
        return toResponse(parcel, isSender ? email : null);
    }

    // ── Mapper ────────────────────────────────────────────────────────

    private ParcelResponse toResponse(Parcel p, String viewerEmail) {
        User s = p.getSender();
        PassengerSummaryResponse senderDto = PassengerSummaryResponse.builder()
            .id(s.getId()).name(s.getName())
            .photoUrl(userMapper.toUrl(s.getPhotoPath()))
            .gender(s.getGender())
            .avgRating(s.getAvgRating()).totalRides(s.getTotalRides())
            .build();

        // Pickup OTP: only shown to the sender when status is ACCEPTED or later
        String pickupOtp = null;
        if (viewerEmail != null
                && s.getEmail().equals(viewerEmail)
                && (p.getStatus() == ParcelStatus.ACCEPTED
                    || p.getStatus() == ParcelStatus.IN_TRANSIT)) {
            pickupOtp = p.getPickupOtp();
        }

        // Delivery OTP: only shown to the sender when status is IN_TRANSIT
        String deliveryOtp = null;
        if (viewerEmail != null
                && s.getEmail().equals(viewerEmail)
                && p.getStatus() == ParcelStatus.IN_TRANSIT) {
            deliveryOtp = p.getDeliveryOtp();
        }

        long unread = 0;
        if (viewerEmail != null) {
            User viewer = userRepository.findByEmail(viewerEmail).orElse(null);
            if (viewer != null) {
                unread = messageRepository.countByParcelIdAndReadFalseAndSenderIdNot(
                    p.getId(), viewer.getId());
            }
        }

        return ParcelResponse.builder()
            .id(p.getId())
            .sender(senderDto)
            .ride(rideService.toResponse(p.getRide()))
            .fromLocation(p.getFromLocation())
            .fromLat(p.getFromLat())
            .fromLng(p.getFromLng())
            .toLocation(p.getToLocation())
            .toLat(p.getToLat())
            .toLng(p.getToLng())
            .size(p.getSize())
            .description(p.getDescription())
            .photoUrl(userMapper.toUrl(p.getPhotoPath()))
            .price(p.getPrice())
            .restrictedItemsAcknowledged(p.isRestrictedItemsAcknowledged())
            .status(p.getStatus())
            .pickupOtp(pickupOtp)
            .pickupOtpVerified(p.isPickupOtpVerified())
            .beforePhotoUrl(userMapper.toUrl(p.getBeforePhotoPath()))
            .deliveryOtp(deliveryOtp)
            .deliveryOtpVerified(p.isDeliveryOtpVerified())
            .afterPhotoUrl(userMapper.toUrl(p.getAfterPhotoPath()))
            .createdAt(p.getCreatedAt())
            .unreadMessages(unread)
            .build();
    }

    private ParcelComplaintResponse toComplaintResponse(ParcelComplaint c) {
        return ParcelComplaintResponse.builder()
            .id(c.getId())
            .parcelId(c.getParcel().getId())
            .raisedById(c.getRaisedBy().getId())
            .raisedByName(c.getRaisedBy().getName())
            .reason(c.getReason())
            .resolution(c.getResolution())
            .createdAt(c.getCreatedAt())
            .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private Parcel findParcelForDriver(Long parcelId, String driverEmail) {
        User   driver = findUser(driverEmail);
        Parcel parcel = findParcel(parcelId);
        if (!parcel.getRide().getDriver().getId().equals(driver.getId())) {
            throw new AppException("Not your parcel to manage.", HttpStatus.FORBIDDEN);
        }
        return parcel;
    }

    private Parcel findParcel(Long id) {
        return parcelRepository.findById(id)
            .orElseThrow(() -> new AppException("Parcel not found.", HttpStatus.NOT_FOUND));
    }

    private User findVerifiedUser(String email) {
        User u = findUser(email);
        if (u.getStatus() != com.ridemate.enums.UserStatus.VERIFIED) {
            throw new AppException("Your account must be verified.", HttpStatus.FORBIDDEN);
        }
        return u;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }

    private void assertParcelStatus(Parcel p, ParcelStatus expected) {
        if (p.getStatus() != expected) {
            throw new AppException(
                "Parcel must be in " + expected + " state. Current: " + p.getStatus(),
                HttpStatus.BAD_REQUEST);
        }
    }
}
