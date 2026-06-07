package com.ridemate.repository;

import com.ridemate.entity.User;
import com.ridemate.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminUserRepository extends JpaRepository<User, Long> {

    /** All users with PENDING_VERIFICATION status, newest first */
    Page<User> findByStatusOrderByCreatedAtAsc(UserStatus status, Pageable pageable);

    /** Search users by name or email (case-insensitive) */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<User> searchUsers(String q, Pageable pageable);

    long countByStatus(UserStatus status);
}
