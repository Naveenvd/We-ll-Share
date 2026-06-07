package com.ridemate.repository;

import com.ridemate.entity.ParcelComplaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelComplaintRepository extends JpaRepository<ParcelComplaint, Long> {

    List<ParcelComplaint> findByParcelId(Long parcelId);

    boolean existsByParcelId(Long parcelId);

    /** Counts unresolved complaints (where admin has not written a resolution note). */
    long countByResolutionIsNull();
}
