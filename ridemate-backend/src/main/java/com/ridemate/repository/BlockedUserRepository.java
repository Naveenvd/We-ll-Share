package com.ridemate.repository;

import com.ridemate.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {

    List<BlockedUser> findByBlockerIdOrderByCreatedAtDesc(Long blockerId);

    Optional<BlockedUser> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /** True if either party has blocked the other */
    @Query("""
        SELECT COUNT(b) > 0 FROM BlockedUser b
        WHERE (b.blocker.id = :userId1 AND b.blocked.id = :userId2)
           OR (b.blocker.id = :userId2 AND b.blocked.id = :userId1)
    """)
    boolean isEitherBlocked(Long userId1, Long userId2);
}
