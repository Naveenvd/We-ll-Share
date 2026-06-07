package com.ridemate.service;

import com.ridemate.dto.request.*;
import com.ridemate.dto.response.*;
import com.ridemate.entity.*;
import com.ridemate.exception.AppException;
import com.ridemate.repository.*;
import com.ridemate.util.FileStorageUtil;
import com.ridemate.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository           userRepository;
    private final VehicleRepository        vehicleRepository;
    private final EmergencyContactRepository ecRepository;
    private final FileStorageUtil          fileUtil;
    private final UserMapper               mapper;

    // ── Profile ────────────────────────────────────────────────────

    public UserProfileResponse getProfile(String email) {
        return mapper.toProfile(findUser(email));
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = findUser(email);
        user.setName(req.getName());
        user.setGender(req.getGender());
        user.setDob(req.getDob());
        return mapper.toProfile(userRepository.save(user));
    }

    /** Replace profile photo */
    @Transactional
    public UserProfileResponse uploadPhoto(String email, MultipartFile file) {
        User user = findUser(email);
        // Delete old photo if present
        fileUtil.delete(user.getPhotoPath());
        String path = fileUtil.storeImage(file, "profiles");
        user.setPhotoPath(path);
        return mapper.toProfile(userRepository.save(user));
    }

    // ── Document uploads ───────────────────────────────────────────

    /**
     * Upload Aadhaar document.
     * The number is sent as a form field alongside the file.
     * Re-uploading is allowed but resets status to PENDING_VERIFICATION
     * so admin must re-review.
     */
    @Transactional
    public UserProfileResponse uploadAadhaar(String email,
                                              AadhaarUploadRequest meta,
                                              MultipartFile file) {
        User user = findUser(email);
        fileUtil.delete(user.getAadhaarDocPath());
        String path = fileUtil.storeDocument(file, "aadhaar");
        user.setAadhaarNumber(meta.getAadhaarNumber());
        user.setAadhaarDocPath(path);
        resetToPending(user);
        return mapper.toProfile(userRepository.save(user));
    }

    /**
     * Upload Driving Licence document.
     * Re-uploading resets status to PENDING_VERIFICATION.
     */
    @Transactional
    public UserProfileResponse uploadDl(String email,
                                         DlUploadRequest meta,
                                         MultipartFile file) {
        User user = findUser(email);
        fileUtil.delete(user.getDlDocPath());
        String path = fileUtil.storeDocument(file, "dl");
        user.setDlNumber(meta.getDlNumber());
        user.setDlDocPath(path);
        resetToPending(user);
        return mapper.toProfile(userRepository.save(user));
    }

    // ── Vehicles ───────────────────────────────────────────────────

    public List<VehicleResponse> getVehicles(String email) {
        Long uid = findUser(email).getId();
        return vehicleRepository.findByUserId(uid)
                .stream().map(this::toVehicleResponse).toList();
    }

    @Transactional
    public VehicleResponse addVehicle(String email, VehicleRequest req) {
        User user = findUser(email);
        Vehicle v = Vehicle.builder()
                .user(user)
                .model(req.getModel())
                .numberPlate(req.getNumberPlate().toUpperCase())
                .color(req.getColor())
                .seats(req.getSeats())
                .build();
        return toVehicleResponse(vehicleRepository.save(v));
    }

    @Transactional
    public VehicleResponse updateVehicle(String email, Long vehicleId, VehicleRequest req) {
        Long uid = findUser(email).getId();
        Vehicle v = vehicleRepository.findByIdAndUserId(vehicleId, uid)
                .orElseThrow(() -> new AppException("Vehicle not found.", HttpStatus.NOT_FOUND));
        v.setModel(req.getModel());
        v.setNumberPlate(req.getNumberPlate().toUpperCase());
        v.setColor(req.getColor());
        v.setSeats(req.getSeats());
        return toVehicleResponse(vehicleRepository.save(v));
    }

    @Transactional
    public ApiResponse deleteVehicle(String email, Long vehicleId) {
        Long uid = findUser(email).getId();
        Vehicle v = vehicleRepository.findByIdAndUserId(vehicleId, uid)
                .orElseThrow(() -> new AppException("Vehicle not found.", HttpStatus.NOT_FOUND));
        vehicleRepository.delete(v);
        return ApiResponse.ok("Vehicle removed.");
    }

    // ── Emergency Contacts ─────────────────────────────────────────

    public List<EmergencyContactResponse> getEmergencyContacts(String email) {
        Long uid = findUser(email).getId();
        return ecRepository.findByUserId(uid).stream().map(this::toEcResponse).toList();
    }

    @Transactional
    public EmergencyContactResponse addEmergencyContact(String email, EmergencyContactRequest req) {
        User user = findUser(email);
        if (ecRepository.countByUserId(user.getId()) >= 3) {
            throw new AppException("You can add at most 3 emergency contacts.", HttpStatus.BAD_REQUEST);
        }
        EmergencyContact ec = EmergencyContact.builder()
                .user(user)
                .name(req.getName())
                .phone(req.getPhone())
                .relation(req.getRelation())
                .build();
        return toEcResponse(ecRepository.save(ec));
    }

    @Transactional
    public EmergencyContactResponse updateEmergencyContact(String email,
                                                            Long ecId,
                                                            EmergencyContactRequest req) {
        Long uid = findUser(email).getId();
        EmergencyContact ec = ecRepository.findByIdAndUserId(ecId, uid)
                .orElseThrow(() -> new AppException("Emergency contact not found.", HttpStatus.NOT_FOUND));
        ec.setName(req.getName());
        ec.setPhone(req.getPhone());
        ec.setRelation(req.getRelation());
        return toEcResponse(ecRepository.save(ec));
    }

    @Transactional
    public ApiResponse deleteEmergencyContact(String email, Long ecId) {
        Long uid = findUser(email).getId();
        EmergencyContact ec = ecRepository.findByIdAndUserId(ecId, uid)
                .orElseThrow(() -> new AppException("Emergency contact not found.", HttpStatus.NOT_FOUND));
        ecRepository.delete(ec);
        return ApiResponse.ok("Emergency contact removed.");
    }

    // ── Private helpers ────────────────────────────────────────────

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found.", HttpStatus.NOT_FOUND));
    }

    /**
     * When a user re-uploads verification docs after being rejected or initially,
     * status resets to PENDING_VERIFICATION so admin reviews again.
     */
    private void resetToPending(User user) {
        switch (user.getStatus()) {
            case REJECTED, PENDING_VERIFICATION -> {
                user.setStatus(com.ridemate.enums.UserStatus.PENDING_VERIFICATION);
                user.setRejectionReason(null);
            }
            default -> { /* VERIFIED or SUSPENDED — don't touch status */ }
        }
    }

    private VehicleResponse toVehicleResponse(Vehicle v) {
        return VehicleResponse.builder()
                .id(v.getId()).model(v.getModel())
                .numberPlate(v.getNumberPlate()).color(v.getColor())
                .seats(v.getSeats()).createdAt(v.getCreatedAt())
                .build();
    }

    private EmergencyContactResponse toEcResponse(EmergencyContact ec) {
        return EmergencyContactResponse.builder()
                .id(ec.getId()).name(ec.getName())
                .phone(ec.getPhone()).relation(ec.getRelation())
                .build();
    }
}
