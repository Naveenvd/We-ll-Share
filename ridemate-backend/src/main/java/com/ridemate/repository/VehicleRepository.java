package com.ridemate.repository;

import com.ridemate.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByUserId(Long userId);

    /** Ensure user owns this vehicle before allowing edit/delete */
    Optional<Vehicle> findByIdAndUserId(Long id, Long userId);
}
