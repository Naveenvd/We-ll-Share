package com.ridemate.repository;

import com.ridemate.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByUserId(Long userId);

    long countByUserId(Long userId);

    Optional<EmergencyContact> findByIdAndUserId(Long id, Long userId);
}
